from pprint import pprint

import numpy as np
import torch
import transformers
from torch import nn, optim, FloatTensor, LongTensor, no_grad
import os
import pandas as pd
from torch.nn.functional import normalize
from torch.utils.data.dataset import Dataset as TorchDataset
from tqdm import tqdm
from transformers import BertModel, BertTokenizer, AdamW

from Marie.Util.location import TRAINING_DIR, DEPLOYMENT_DIR, DATA_DIR

# TODO: a Dataset class that provides question examples and their relation
# TODO: also provides a rel embedding

max_len = 12


class TransERelPredictionModel(nn.Module):

    def __init__(self, device=torch.device("cpu"), for_training=False, dataset_dir="CrossGraph/pubchem", dim=20):
        super(TransERelPredictionModel, self).__init__()
        self.dim = dim
        self.dataset_dir = dataset_dir
        self.device = device
        self.bert = BertModel.from_pretrained('bert-base-cased')
        self.dropout = nn.Dropout(0)
        self.linear = nn.Linear(768, self.dim)  # keep this model ...
        self.mid_2 = nn.Linear(512, self.dim)
        self.criterion = torch.nn.CosineEmbeddingLoss()
        self.for_training = for_training
        if self.for_training:
            self.model_dir = DATA_DIR
        else:
            self.model_dir = DATA_DIR

    def load_model(self, model_name):
        print(" - Loading pretrained BERT Mapping model")
        self.load_state_dict(
            torch.load(os.path.join(self.model_dir, self.dataset_dir, model_name), map_location=self.device))

    def distance(self, emb_1, emb_2):
        """
        Calculate the simple manhattan distance between emb_1 and emb_2
        :param emb_1: projected rel
        :param emb_2: original rel
        :return: distance
        """

        assert emb_1.shape[1] == emb_2.shape[1]
        distance = (emb_1 - emb_2).norm(p=1, dim=1)
        return distance

    def predict(self, question):
        with no_grad():
            input_ids = torch.reshape(question['input_ids'], (-1, max_len)).to(self.device)
            attention_mask = torch.reshape(question['attention_mask'], (-1, max_len)).to(self.device)
            pooled_output = self.bert(input_ids=input_ids,
                                      attention_mask=attention_mask,
                                      return_dict=False)[1].to(self.device)
            dropout_output = self.dropout(pooled_output.to(self.device)).to(self.device)
            linear_output = self.linear(dropout_output.to(self.device)).to(self.device)
            return linear_output

    def forward(self, question, y):
        input_ids = torch.reshape(question['input_ids'], (-1, max_len))
        attention_mask = torch.reshape(question['attention_mask'], (-1, max_len))
        pooled_output = self.bert(input_ids=input_ids.to(self.device),
                                  attention_mask=attention_mask.to(self.device),
                                  return_dict=False)[1]

        dropout_output = self.dropout(pooled_output)
        linear_output = self.linear(dropout_output)
        distance = self.distance(linear_output, y)
        '''
        Update the loss function to do a calculation eh + r = et, where r is the output of linear_output ... 
        If the fine-tuning of the current thing fails 
        '''

        return distance, normalize(linear_output)
