import json
import os

import pandas as pd
import torch

from Marie.Util.CommonTools.FileLoader import FileLoader
from Marie.Util.NHopExtractor import HopExtractor
from Marie.Util.location import DATA_DIR


class TransRInferenceDataset(torch.utils.data.Dataset):
    """

    """

    def __init__(self, df, full_dataset_dir, ontology, mode="train"):
        super(TransRInferenceDataset, self).__init__()
        self.full_dataset_dir = full_dataset_dir
        self.ontology = ontology
        self.file_loader = FileLoader(full_dataset_dir=self.full_dataset_dir, dataset_name=self.ontology)
        self.entity2idx, self.idx2entity, self.rel2idx, self.idx2rel = self.file_loader.load_index_files()
        self.neg_sample_dict_path = os.path.join(self.full_dataset_dir, "neg_sample_dict.json")
        self.neg_sample_dict = json.loads(open(self.neg_sample_dict_path).read())
        self.candidate_dict_path = os.path.join(self.full_dataset_dir, "candidate_dict.json")
        self.candidate_dict = json.loads(open(self.candidate_dict_path).read())
        self.candidate_max = max([len(v) for k, v in self.candidate_dict.items()])
        self.node_value_dict_path = os.path.join(self.full_dataset_dir, "node_value_dict.json")
        self.node_value_dict = json.loads(open(self.node_value_dict_path).read())
        self.my_extractor = HopExtractor(
            dataset_dir=full_dataset_dir,
            dataset_name=ontology)
        self.mode = mode
        self.df = df
        self.ent_num = len(self.entity2idx.keys())
        self.rel_num = len(self.rel2idx.keys())
        self.use_cached_triples = True

        cached_triple_path = f"{self.full_dataset_dir}/triple_idx_{self.mode}.json"
        if os.path.exists(cached_triple_path) and self.use_cached_triples:
            # the triples are already cached
            self.triples = json.loads(open(cached_triple_path).read())
        else:

            if self.mode == "train":
                self.triples = self.create_all_triples()
                print(f"Number of triples for training: {len(self.triples)}")
            elif self.mode == "numerical":
                self.triples = self.load_numerical_triples()

            elif self.mode == "train_eval":
                self.triples = self.create_train_small_triples_for_evaluation()

            else:
                self.triples = self.create_test_triples()
                print(f"Number of triples for testing: {len(self.triples)}")

            with open(cached_triple_path, "w") as f:
                f.write(json.dumps(self.triples))
                f.close()

    def create_train_small_triples_for_evaluation(self):
        triples = []
        tail_all = range(0, self.ent_num)
        counter = 0
        for idx, row in self.df.iterrows():
            counter += 1
            print(f"Small train triples: {counter} out of {len(self.df)}")
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            o = self.entity2idx[row[2]]
            for tail in tail_all:
                triple_idx_string = f"{s}_{p}_{tail}"
                if o == tail:
                    triples.append((s, p, tail, o))
                elif not self.my_extractor.check_triple_existence(triple_idx_string):
                    triples.append((s, p, tail, o))
                else:
                    triples.append((s, p, -1, o))

        return triples

    def load_numerical_triples(self):
        triples = []
        for idx, row in self.df.iterrows():
            # print(f"{idx} out of {len(self.df)}")
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            v = float(row[2])
            true_triple = (s, p, v)
            triples.append(true_triple)
        return triples

    def create_fake_triple(self, s, p, o, mode="head"):
        s_p_str = f'{s}_{p}'
        fake_candidates = self.neg_sample_dict[s_p_str]
        return fake_candidates

    def create_test_triples(self):
        triples = []
        for idx, test_row in self.df.iterrows():
            # print(f"{idx} out of {len(self.df)}")
            s = self.entity2idx[test_row[0]]
            p = self.rel2idx[test_row[1]]
            o = self.entity2idx[test_row[2]]
            if test_row[1] == "hasRole":
                candidates = self.candidate_dict[str(o)]
                length_diff = self.candidate_max - len(candidates)
                padding_list = [-1] * length_diff
                candidates += padding_list
                for tail in candidates:
                    triple_idx_string = f"{s}_{p}_{tail}"
                    if o == tail:
                        triples.append((s, p, tail, o))
                    elif not self.my_extractor.check_triple_existence(triple_idx_string):
                        triples.append((s, p, tail, o))
                    else:
                        triples.append((s, p, -1, o))

        return triples

    def create_all_triples(self):
        triples = []
        counter = 0
        for idx, row in self.df.iterrows():
            counter += 1
            print(f"Train triples {counter} out of {len(self.df)}")
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            o = self.entity2idx[row[2]]
            if row[1] == "hasMolecularWeight":
                true_triple = (s, p, o, self.node_value_dict[row[2]])
            else:
                true_triple = (s, p, o, -999)

            fake_tails = self.create_fake_triple(s, p, o, mode="head")
            for fake_tail in fake_tails:
                fake_triple = (s, p, fake_tail)
                triples.append((true_triple, fake_triple))

        return triples

    def create_inference_test_sample(self, reaction_triples):
        """
        Given the triples for test, we try the following evaluation, use the reactants embeddings r to calculate the
        embedding of the reaction R' which is not observed in the training set.
        Then use R' to infer the products ...
        Then reverse the process to infer reactants given products ...
        :return: list of tuples (S_r1, isReactant), (S_r2, isReactant) -> (S_p1), (S_p2)
        """
        triples = []
        for reaction in reaction_triples:
            data = reaction_triples[reaction]
            r_idx_list = [self.entity2idx[r] for r in data['reactants']]
            p_idx_list = [self.entity2idx[p] for p in data['products']]
            triples.append((r_idx_list, p_idx_list))
        return triples

    def __len__(self):
        return len(self.triples)

    def __getitem__(self, item):
        return self.triples[item]


if __name__ == "__main__":
    sub_ontology = "role_with_subclass_mass"
    full_dir = os.path.join(DATA_DIR, 'CrossGraph', f'ontospecies_new/{sub_ontology}')
    # ===================================================================================================
    df_train = pd.read_csv(os.path.join(full_dir, f"{sub_ontology}-train-2.txt"), sep="\t", header=None)
    df_train = df_train.sample(frac=0.001)
    df_test = pd.read_csv(os.path.join(full_dir, f"{sub_ontology}-test.txt"), sep="\t", header=None)
    df_numerical = pd.read_csv(os.path.join(full_dir, f"{sub_ontology}-numerical.txt"), sep="\t", header=None)
    # ===================================================================================================
    train_set = TransRInferenceDataset(df_train, full_dataset_dir=full_dir, ontology=sub_ontology, mode="train")
    # test_set = TransRInferenceDataset(df_test, full_dataset_dir=full_dir, ontology=sub_ontology, mode="test")
    # eval_set = TransRInferenceDataset(df_test, full_dataset_dir=full_dir, ontology=sub_ontology, mode="train_eval")
    # numerical_set = TransRInferenceDataset(df_numerical, full_dataset_dir=full_dir, ontology=sub_ontology,
    #                                        mode="numerical")

    # test_dataloader = torch.utils.data.DataLoader(test_set, batch_size=test_set.candidate_max, shuffle=False)
    train_dataloader = torch.utils.data.DataLoader(train_set, batch_size=32, shuffle=True)
    # numerical_dataloader = torch.utils.data.DataLoader(numerical_set, batch_size=32, shuffle=True)
    # eval_set_dataloader = torch.utils.data.DataLoader(eval_set, batch_size=eval_set.ent_num, shuffle=False)

    for pos, neg in train_dataloader:
        numerical_idx_list = (pos[3] != -999)
        pos = torch.transpose(torch.stack(pos), 0, 1)
        pos_numerical = torch.transpose(pos[numerical_idx_list], 0, 1)
        pos_non_numerical = torch.transpose(pos[~numerical_idx_list], 0, 1)  # create negative index list with ~
        neg = torch.transpose(torch.stack(neg), 0, 1)
        neg_numerical = torch.transpose(neg[numerical_idx_list], 0, 1)
        neg_non_numerical = torch.transpose(neg[~numerical_idx_list], 0, 1)
        print(pos_numerical, neg_numerical)
        x = input()

    # for row in eval_set_dataloader:
    #     # print(row)
    #     heads = row[0]
    #     rels = row[1]
    #     all_tails = row[2]
    #     true_tail = row[3][0]
    #     print(heads, rels, all_tails, true_tail)
    # print(len(all_tails))
    # print(len(heads))
    # print(len(rels))
    # selected_idx = (all_tails >= 0)
    # heads = heads[selected_idx]
    # rels = rels[selected_idx]
    # print(len(selected_idx))
    # print(len(heads))
    # print(len(rels))
    # print("============")

    # for row in train_dataloader:
    #     pass
    #
    # for row in test_dataloader:
    #     pass
    #
    # for row in numerical_dataloader:
    #     print(row)

    # for row in test_dataloader:
    #     reactants = [s.item() for s in row[0]]
    #     products = [s.item() for s in row[1]]
    #     print(reactants)
    #     print(products)
    #     print("--------")
