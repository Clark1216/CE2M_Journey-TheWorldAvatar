import os
import unittest
import torch

from Marie.Util.CommonTools.NLPTools import NLPTools
from Marie.Util.Models.CrossGraphAlignmentModel import CrossGraphAlignmentModel
from Marie.Util.location import DATA_DIR


class TestCrossGraph(unittest.TestCase):

    def test(self):
        label_dict = {"pubchem": 0, "ontocompchem": 1, "ontospecies": 2,
                      "ontokin": 3, "wikidata": 4, "ontospecies_new": 5,
                      "ontoagent": 6, "ontomops": 7, "ontokin_reaction": 8}
        label_list = list(label_dict.keys())
        use_cuda = torch.cuda.is_available()
        self.device = torch.device("cuda" if use_cuda else "cpu")
        print(f'=========== USING {self.device} ===============')
        self.model = CrossGraphAlignmentModel(device=self.device).to(self.device)
        nlp = NLPTools(tokenizer_name="bert-base-uncased")
        my_alignment_model = CrossGraphAlignmentModel(device="cpu")
        dataset_path = os.path.join(DATA_DIR, "CrossGraph/cross_graph_model_with_all_9")
        my_alignment_model.load_state_dict(torch.load(dataset_path, map_location="cpu"))
        questions = ["molar mass",
                     "boiling point",
                     "geometry",
                     "vapour pressure",
                     "chemical structure",
                     "inventor",
                     "some random bullshit",
                     "flash point",
                     "location discovery",
                     "reactions",
                     "heat capacity",
                     "mops with assembly model",
                     "cbu with shape",
                     "logs",
                     "logp"]


        for q in questions:
            predicted_domain_labels = []
            _, tokenized_q = nlp.tokenize_question(q, 1)
            pred_domain_list = my_alignment_model.predict_domain([tokenized_q])[0]
            for idx, domain in enumerate(pred_domain_list):
                if domain == 1:
                    predicted_domain_labels.append(label_list[idx])
            print("question", q)
            print("domain", predicted_domain_labels)

        # print(f"Question: {q} \n Predicted domain: {pred_domain}")
        # print(pred_domain.repeat(5, 1))


if __name__ == "__main__":
    my_test = TestCrossGraph()
    my_test.test()
