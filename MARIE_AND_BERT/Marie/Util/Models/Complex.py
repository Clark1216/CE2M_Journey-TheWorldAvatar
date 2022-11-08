import pickle
import sys

import pandas
import os
import torch
from torch import nn
import torch.nn.functional as F
from torch.nn.init import xavier_uniform_

sys.path.append("../../..")

from Marie.Util.Embedding.EmbeddingTrainer import Trainer
from Marie.Util.location import DATA_DIR
from KGToolbox.NHopExtractor import HopExtractor


class Complex(nn.Module):

    def __init__(self, dim, ent_num, rel_num, resume_training=False, device='cpu', dataset_dir=None):
        super(Complex, self).__init__()
        self.dim = dim
        self.ent_num = ent_num
        self.rel_num = rel_num
        self.device = device
        self.dataset_dir = dataset_dir

        if resume_training:
            self.re_ent, self.im_ent = self.load_embedding(embedding_name="ent_embedding.tsv")
            self.re_rel, self.im_rel = self.load_embedding(embedding_name="rel_embedding.tsv")

        else:
            self.re_rel = self._init_rel_embedding()
            self.im_rel = self._init_rel_embedding()
            self.re_ent = self._init_ent_embedding()
            self.im_ent = self._init_ent_embedding()

    def _init_ent_embedding(self):
        """
        :param dim: dimension of the embedding
        :param num: number of entities
        :return:
        """
        entity_embeddings = nn.Embedding(num_embeddings=self.ent_num + 1, embedding_dim=self.dim)
        xavier_uniform_(entity_embeddings.weight.data)
        return entity_embeddings

    def _init_rel_embedding(self):
        rel_embeddings = nn.Embedding(num_embeddings=self.rel_num + 1, embedding_dim=self.dim)
        xavier_uniform_(rel_embeddings.weight.data)
        return rel_embeddings

    def load_embedding(self, embedding_name):
        tsv_file = pandas.read_csv(os.path.join(self.dataset_dir, embedding_name), sep='\t', header=None)
        re, im = tsv_file.iloc[:, : self.dim], tsv_file.iloc[:, self.dim:]
        re = torch.FloatTensor(re.values)
        im = torch.FloatTensor(im.values)
        return nn.Embedding.from_pretrained(re).requires_grad_(True), \
               nn.Embedding.from_pretrained(im).requires_grad_(True)

    def forward(self, triples):
        target = triples[3]
        pred = self.score(triples).to(self.device)
        return self.pointwise_bce(pred.type(torch.FloatTensor), target.type(torch.FloatTensor))

    def predict(self, triple):
        return self.score(triple)

    def pointwise_bce(self, preds, target):
        # split the
        loss = torch.nn.BCEWithLogitsLoss()(torch.clamp(preds, min=0.0, max=1.0),
                                            target)  # torch.clamp(target, min=0.0, max=1.0))
        return loss

    def score(self, triple):
        head_idx = triple[0]
        rel_idx = triple[1]
        tail_idx = triple[2]

        re_head = self.re_ent(head_idx)
        im_head = self.im_ent(head_idx)

        re_rel = self.re_rel(rel_idx)
        im_rel = self.re_rel(rel_idx)

        re_tail = self.re_ent(tail_idx)
        im_tail = self.im_ent(tail_idx)

        pred = - torch.sum(re_head * re_tail * re_rel + im_head * im_tail * re_rel +
                           re_head * im_tail * im_rel - im_head * re_tail * im_rel, -1)

        return pred


if __name__ == '__main__':
    full_dir = os.path.join(DATA_DIR, 'ontocompchem_calculation')
    r2i_path = open(os.path.join(full_dir, f'relation2idx.pkl'), 'rb')
    e2i_path = open(os.path.join(full_dir, f'entity2idx.pkl'), 'rb')
    rel_num = len(pickle.load(r2i_path).keys())
    ent_num = len(pickle.load(e2i_path).keys())
    model = Complex(dim=80, rel_num=rel_num, ent_num=ent_num,
                    dataset_dir=os.path.join(DATA_DIR, "ontocompchem_calculation"), resume_training=False)
    trainer = Trainer(model=model, dataset_name='ontocompchem_calculation', epochs=1000, learning_rate=1,
                      pointwise=True, batch_size=32, save_model=False, complex=True, gamma=0.99)
    trainer.train()
