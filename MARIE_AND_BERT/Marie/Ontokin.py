from KGToolbox.NHopExtractor import HopExtractor
from Marie.EntityLinking.ChemicalNEL import ChemicalNEL
from Marie.QAEngine import QAEngine
from Marie.Util.Models.TransEScoreModel import TransEScoreModel


class OntoKinQAEngine(QAEngine):

    def __init__(self, dataset_dir="CrossGraph/ontokin", dataset_name="ontokin"):
        super().__init__(dataset_dir, dataset_name)


if __name__ == "__main__":
    my_engine = OntoKinQAEngine(dataset_dir="CrossGraph/ontokin", dataset_name="ontokin")
    rst = my_engine.run("what is the chemical formula of CO2")
    print(rst)
