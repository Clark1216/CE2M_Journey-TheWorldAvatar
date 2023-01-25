import json
import random

import pandas as pd
import os, sys
sys.path.append("..")

# from Marie.CrossGraphQAEngine import CrossGraphQAEngine
# from Marie.OntoSpecies import OntoSpeciesQAEngine
# from Marie.PubChem import PubChemEngine
# from Marie.OntoCompChem import OntoCompChemEngine
# from Marie.Ontokin import OntoKinQAEngine
# from Marie.CrossGraphQAEngine import CrossGraphQAEngine
from Marie.Util.location import DATA_DIR


def hit_k_rate(true_answer, pred_answers):
    scores = []
    k_list = [1, 5, 10]
    for k in k_list:
        top_k_answers = pred_answers[0: min(k, len(pred_answers))]
        if true_answer in top_k_answers:
            scores.append(1)
        else:
            scores.append(0)
    return scores


class Evaluator:

    def __init__(self):
        self.dataset_dir = os.path.join(DATA_DIR, 'CrossGraph')
        # self.pubchem_engine = PubChemEngine()
        # self.ontospecies_engine = OntoSpeciesQAEngine()
        # self.ontocompchem_engine = OntoCompChemEngine()
        # self.ontokin_engine = OntoKinQAEngine()
        self.cross_graph_test_path = os.path.join(self.dataset_dir, "cross_graph_test.tsv")
        self.cross_graph_test = pd.read_csv(self.cross_graph_test_path, sep="\t")
        # question	heads	domains	answers	mention
        self.cross_graph_test["heads"] = self.cross_graph_test["heads"].apply(eval)
        self.cross_graph_test["domains"] = self.cross_graph_test["domains"].apply(eval)
        self.cross_graph_test["answers"] = self.cross_graph_test["answers"].apply(eval)

    def calculate_accuracy(self, pred_answers, true_answers):
        """
        Calculate both hit-k rate and the recall-precision-f1 scores ...
        :param pred_answers:
        :param true_answers:
        :return:
        """
        counter = 0
        hit_1, hit_5, hit_10, mrr = 0, 0, 0, 0
        # Calculate the hit-k rate of the true answers
        for true_ans in true_answers:
            # remove other true answers from pred_answers
            other_true_answers = [t_a for t_a in true_answers if t_a != true_ans]
            filtered_predicted_answer = [p_a for p_a in pred_answers if p_a not in other_true_answers]
            counter += 1
            one_hit_1, one_hit_5, one_hit_10 = hit_k_rate(true_answer=true_ans, pred_answers=filtered_predicted_answer)
            # print('---------')
            # print("filtered_predicted_answer", filtered_predicted_answer)
            # print("true answer: ", true_ans)
            # print("HIT MATRIX: ", one_hit_1, one_hit_5, one_hit_10)
            hit_1 += one_hit_1
            hit_5 += one_hit_5
            hit_10 += one_hit_10
            if true_ans in filtered_predicted_answer:
                rr = 1 / (filtered_predicted_answer.index(true_ans) + 1)
            else:
                rr = 0
            mrr += rr

        return hit_1, hit_5, hit_10, counter, mrr

    def test_cross_graph(self):
        total_hit_1, total_hit_5, total_hit_10, total_counter, total_mrr = 0, 0, 0, 0, 0
        df_test = self.cross_graph_test
        answer_dict_path = os.path.join(self.dataset_dir, "answer_dict_nel_ablation.json")
        if os.path.exists(os.path.join(answer_dict_path)):
            answer_dict = json.loads(open(answer_dict_path).read())
            for question, answers in answer_dict.items():
                # find out the true_answers via test set
                predicted_answers = [d['node'] for d in answers if 'node' in d]
                predicted_mention = [d['target'] for d in answers if 'target' in d][0].lower()
                question_row = df_test.loc[df_test['question'] == question]
                true_domains = question_row["domains"].tolist()
                true_answers = question_row["answers"].tolist()[0]
                true_mention = question_row.iloc[0]["mention"].lower()
                print("true mention", true_mention)
                print("predicted_mention", predicted_mention)
                if "EMPTY SLOT" not in predicted_answers and predicted_mention == true_mention:
                    print(question)
                    print(predicted_answers)
                    print(true_answers)
                    hit_1, hit_5, hit_10, counter, mrr = \
                        self.calculate_accuracy(pred_answers=predicted_answers, true_answers=true_answers)
                    total_hit_1 += hit_1
                    total_hit_5 += hit_5
                    total_hit_10 += hit_10
                    total_mrr += mrr
                    total_counter += counter
                    print("-=====================")

            print("total hit 1 rate ", total_hit_1 / total_counter)
            print("total hit 5 rate", total_hit_5 / total_counter)
            print("total hit 10 rate ", total_hit_10 / total_counter)
            print("mrr ", total_mrr / total_counter)
            print("=====================")
        else:
            answer_dict = {}
            engine = CrossGraphQAEngine()
            for idx, row in self.cross_graph_test.iterrows():
                _, question, heads, domains, answers, mention = row
                heads = {}
                for h, d in zip(heads, domains):
                    heads[d] = h
                answers = engine.selected_ontology_questions(question, disable_alignment=True, heads=heads)
                answer_dict[question] = answers

            with open(answer_dict_path, 'w') as f:
                f.write(json.dumps(answer_dict))

    def test_wikidata_normal(self):
        total_hit_1, total_hit_5, total_hit_10, total_mrr = 0, 0, 0, 0
        f_total_hit_1, f_total_hit_5, f_total_hit_10, f_total_mrr = 0, 0, 0, 0
        nel_hit_rate = 0
        operator_hit_rate = 0
        p_hit_rate = 0
        filter_counter = 0
        total_counter = 0
        result_set_path = os.path.join(DATA_DIR, self.dataset_dir, "wikidata_numerical"
                                                                   "/wikidata_numerical_test_result_normal.json")

        result_set_smaller_path = os.path.join(DATA_DIR, self.dataset_dir, "wikidata_numerical"
                                                                           "/wikidata_numerical_test_result_normal_smaller.json")
        test_set_path = os.path.join(DATA_DIR, self.dataset_dir, "wikidata_numerical/"
                                                                 "wikidata_numerical_test_set-normal.json")

        test_set_smaller_path = os.path.join(DATA_DIR, self.dataset_dir, "wikidata_numerical/"
                                                                         "wikidata_numerical_test_set-normal_smaller.json")

        result_set = json.loads(open(result_set_path).read())
        result_set_smaller = json.loads(open(result_set_smaller_path).read())
        test_set = json.loads(open(test_set_path).read())
        test_set_smaller = json.loads(open(test_set_smaller_path).read())

        for result_row, test_row in zip(result_set_smaller, test_set_smaller):
            pred_operator, pred_tails, nel_hit, _, pred_p = result_row
            question, true_head, true_p, true_tail, _, _, _, _ = test_row
            if nel_hit:
                filter_counter += 1
                nel_hit_rate += 1
                f_hit_1, f_hit_5, f_hit_10, f_counter, f_mrr = \
                    self.calculate_accuracy(pred_answers=pred_tails, true_answers=[true_tail])
                f_total_hit_1 += f_hit_1
                f_total_hit_5 += f_hit_5
                f_total_hit_10 += f_hit_10
                f_total_mrr += f_mrr

                if pred_p == true_p:
                    p_hit_rate += 1

            hit_1, hit_5, hit_10, counter, mrr = \
                self.calculate_accuracy(pred_answers=pred_tails, true_answers=[true_tail])
            total_hit_1 += hit_1
            total_hit_5 += hit_5
            total_hit_10 += hit_10
            total_mrr += mrr
            if hit_1 == 1:
                print(question)
            if pred_operator == "none":
                operator_hit_rate += 1

        nel_hit_rate = nel_hit_rate / len(result_set_smaller)
        print("nel_hit_rate", nel_hit_rate)
        operator_hit_rate = operator_hit_rate / len(result_set_smaller)
        print("operator_hit_rate", operator_hit_rate)
        print("p_hit_rate", p_hit_rate / filter_counter)
        print(f"hit 1 rate {total_hit_1 / len(result_set_smaller)}")
        print(f"hit 5 rate {total_hit_5 / len(result_set_smaller)}")
        print(f"hit 10 rate {total_hit_10 / len(result_set_smaller)}")
        print(f"mrr {total_mrr / len(result_set_smaller)}")

        print(f"filtered hit 1 rate {f_total_hit_1 / filter_counter}")
        print(f"filtered hit 5 rate {f_total_hit_5 / filter_counter}")
        print(f"filtered hit 10 rate {f_total_hit_10 / filter_counter}")
        print(f"filtered mrr {f_total_mrr / filter_counter}")

    def value_lookup(self, value_dictionary, tail):
        if tail in value_dictionary:
            return value_dictionary[tail]
        else:
            return None

    def test_numerical(self):
        value_dictionary_path = os.path.join(DATA_DIR, "CrossGraph/wikidata_numerical",
                                             f"wikidata_numerical_value_dict.json")

        value_dictionary = json.loads(open(value_dictionary_path).read())
        numerical_test_set_path = os.path.join(DATA_DIR, self.dataset_dir,
                                               "wikidata_numerical/wikidata_numerical_test_set-numerical.json")
        numerical_result_set_path = os.path.join(DATA_DIR, self.dataset_dir,
                                                 "wikidata_numerical/wikidata_numerical_test_result_3.json")

        with open(numerical_test_set_path, 'r') as f:
            test_set = json.loads(f.read())
        # test_set = json.loads(open(numerical_test_set_path).read())
        result_set = json.loads(open(numerical_result_set_path).read())
        total_recall = 0
        total_precision = 0
        total_recall_filtered = 0
        total_precision_filtered = 0
        total_abs_diff = 0
        total_diff_perc = 0
        operator_accuracy = 0
        p_hit_rate = 0
        total_counter = 0
        for test_row, result_row in zip(test_set, result_set):
            average_abs_diff = 0
            question, _, true_p, true_tails, _, _, true_operator = test_row
            predicted_operator, predicted_tails, filtered_predicted_tails, predicted_numerical_values, pred_p = \
                result_row

            # print("predicted_numerical", predicted_numerical_values)
            # print("true numerical", true_numerical_values)
            # TODO: compare the tails
            # wrong_tails = [p_t for p_t in predicted_tails if p_t not in true_tails]
            # print("wrong_tails", wrong_tails)
            # missed_tails =  [t_t for t_t in true_tails if t_t not in predicted_tails]
            # print("missed_tails", missed_tails)

            if predicted_numerical_values is not None and len(predicted_numerical_values) > 0:
                # 171.43
                true_numerical_values = [self.value_lookup(value_dictionary, o) for o in filtered_predicted_tails]

                num_counter = 0
               #  print(len(predicted_numerical_values))
               #  print(len(true_numerical_values))

                for p_n, t_n in zip(predicted_numerical_values, true_numerical_values):
                    if t_n is not None:
                        average_abs_diff += abs(t_n - p_n)
                        num_counter += 1

                if num_counter == 0:
                    average_abs_diff = 0
                else:
                    average_abs_diff = average_abs_diff / num_counter

                if true_operator == predicted_operator:
                    operator_accuracy += 1

                if pred_p == true_p:
                    p_hit_rate += 1

                total_counter += 1
                # average_diff_perc = sum([diff[1] for diff in predicted_numerical_values]) \
                #                     / len(predicted_numerical_values)

                total_abs_diff += average_abs_diff
                # total_diff_perc += average_diff_perc

                # TP = number of tails in predicted_tail and in true_tails.
                TP = len([tp for tp in predicted_tails if tp in true_tails])
                # FP = number of tails in predicted_tail and not in true_tails
                FP = len([fp for fp in predicted_tails if fp not in true_tails])
                # FN = number of tails in true_tails and not in predicted tails
                FN = len([fn for fn in true_tails if fn not in predicted_tails])

                TP_filtered = len([tp for tp in filtered_predicted_tails if tp in true_tails])
                # FP = number of tails in predicted_tail and not in true_tails
                FP_filtered = len([fp for fp in filtered_predicted_tails if fp not in true_tails])
                # FN = number of tails in true_tails and not in predicted tails
                FN_filtered = len([fn for fn in true_tails if fn not in filtered_predicted_tails])

                if TP == 0:
                    precision = 0
                    recall = 0
                    filtered_precision = 0
                    filtered_recall = 0
                else:
                    precision = TP / (TP + FP)
                    recall = TP / (TP + FN)
                    filtered_precision = TP_filtered / (TP_filtered + FP_filtered)
                    filtered_recall = TP_filtered / (TP_filtered + FN_filtered)
                    if precision > 0.8:
                        print(question)


                total_recall += recall
                total_precision += precision
                total_recall_filtered += filtered_recall
                total_precision_filtered += filtered_precision

        average_recall = total_recall / total_counter
        average_precision = total_precision / total_counter
        average_recall_filtered = total_recall_filtered / total_counter
        average_precision_filtered = total_precision_filtered / total_counter

        f1 = 2 * (average_precision * average_recall) / (average_precision + average_recall)
        f1_filtered = 2 * (average_precision_filtered * average_recall_filtered) / \
                      (average_precision_filtered + average_recall_filtered)

        # print("average_precision", average_precision)
        # print("average_recall", average_recall)
        # print("average_precision_filtered", average_precision_filtered)
        # print("average_recall_filtered", average_recall_filtered)
        # print("f1", f1)
        # print("f1_filtered", f1_filtered)
        # print(total_abs_diff / total_counter)
        # print(total_diff_perc / total_counter)
        # print("total operator accuracy", operator_accuracy / total_counter)
        # print("total p hit rate", p_hit_rate / total_counter)

    def test_1_to_1(self, ontology, engine):
        hit_1 = 0
        hit_5 = 0
        hit_10 = 0
        mrr = 0
        ner_failure = 0
        start_idx = 0
        total_counter = start_idx
        df_test = pd.read_csv(os.path.join(self.dataset_dir, ontology, f'{ontology}_test.tsv'), sep='\t')
        for idx, row in list(df_test.iterrows())[start_idx: -1]:  # 344
            total_counter += 1
            question, head, domain, answer, mention, rel = row
            print(f"========== Evaluating {question} - Number: {total_counter} out of {len(df_test)} ==============")
            try:
                answer_list, score_list, target_list = engine.selected_ontology_questions(question)
                target = target_list[0]
                print("target", target)
                print("mention", mention)
                try:
                    target.lower()
                    if target.lower().strip() != mention.lower().strip():
                        ner_failure += 1
                except AttributeError:
                    # The target is a nan, ner did
                    ner_failure += 1

                if answer in answer_list:
                    answer_index = answer_list.index(answer)
                    if answer_index <= 0:
                        hit_1 += 1
                    elif answer_index <= 4:
                        hit_5 += 1
                    elif answer_index <= min(len(answer_list), 10):
                        hit_10 += 1
                    rr = 1 / (answer_index + 1)
                else:
                    rr = 0

                mrr += rr

            # TODO: check the hit rate of the answer ...
            except ValueError:
                print("failed question", question)
                ner_failure += 1

        hit_5 = hit_5 + hit_1
        hit_10 = hit_10 + hit_5
        print(f"hit 1 number {hit_1}")
        print(f"hit 5 number {hit_5}")
        print(f"hit 10 number {hit_10}")
        # =============================================
        print(f"ner failure number {ner_failure}")
        print(f"hit 1 rate {hit_1 / total_counter}")
        print(f"hit 5 rate {hit_5 / total_counter}")
        print(f"hit 10 rate {hit_10 / total_counter}")
        print(f"mrr {mrr / total_counter}")
        print(f"filtered hit 1 rate {hit_1 / (total_counter - ner_failure)}")
        print(f"filtered hit 5 rate {hit_5 / (total_counter - ner_failure)}")
        print(f"filtered hit 10 rate {hit_10 / (total_counter - ner_failure)}")
        print(f"f_mrr {mrr / (total_counter - ner_failure)}")


if __name__ == "__main__":
    my_evaluator = Evaluator()
    my_evaluator.test_numerical()
    # my_evaluator.test_1_to_1(ontology="ontospecies", engine=my_evaluator.ontospecies_engine)
    # my_evaluator.test_cross_graph()
