from blink.common.params import BlinkParser
from blink.biencoder.eval_biencoder import main

if __name__ == "__main__":
    parser = BlinkParser(add_model_args=True)
    parser.add_eval_args()

    args = parser.parse_args()
    params = args.__dict__
    params['zeshel'] = False
    params['entity_dict_path'] = './data/tbox/tbox.jsonl'
    mode_list = params["mode"].split(',')
    for mode in mode_list:
        new_params = params
        new_params["mode"] = mode
        main(new_params)