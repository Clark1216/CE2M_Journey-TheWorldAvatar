import json, re
from pprint import pprint

from fuzzywuzzy import fuzz
from nltk.tokenize import word_tokenize


def remove_duplicated(uris):
    temp = []
    result = []
    for uri in uris:
        if uri[0] not in temp:
            result.append(uri)
        temp.append(uri[0])
    return result


def filter_components(term_type, term):
    stopwords = ['the', 'all', 'a', 'an', 'that', 'of']
    smaller_than = ['smaller than', 'less', 'less than', 'under', 'smaller', 'beneath', 'lower', 'lower than', 'fewer']
    larger_than = ['bigger', 'bigger than', 'larger than', 'larger', 'over', 'above', 'beyond', 'broader', 'broader ',
                   'than', 'more than','more']

    print('term_type', term_type, 'term', term)
    try:
        term_tokens = word_tokenize(term)
    except:
        return None
    term_processed = ' '.join(
        [token.lower().strip() for token in term_tokens if token.lower().strip() not in stopwords])

    print('\n term_process', term_processed)
    if term_type == 'comparison':
        smaller_score = max([fuzz.ratio(term_processed, word) for word in smaller_than])
        larger_score = max([fuzz.ratio(term_processed, word) for word in larger_than])
        if smaller_score > larger_score:
            return '<'
        else:
            return '>'

    elif term_type == 'numerical_value':
        return re.fullmatch(r'[-0-9.]+', term_processed)[0]
    else:
        return term_processed


class SearchEngine:
    def __init__(self):
        self.file_path = '../search_engine/wiki_dictionary'
        with open(self.file_path) as f:
            self.wiki_dictionary = json.loads(f.read())
        self.top_k = 3

    def compare_other_dictionaries(self, term, intent, mode):
        print('executing the compare other dictionaries')
        print('intent', intent)
        print('mode', mode)
        print('term', term)
        if intent == 'batch_restriction_query':
            dict_names = ['attribute', 'class', 'entity']
            dict_names.remove(mode)
            highest_scores = []
            for name in dict_names:
                table = self.wiki_dictionary[name]['list']
                scores = []
                for word in table:
                    scores.append(fuzz.ratio(term.lower(), word.lower()))
                highest_scores.append(max(scores))
                print('highest_scores', highest_scores)
            return max(highest_scores)
        else:
            return 0

    def find_matches_from_wiki(self, term, mode='entity', intent='item_attribute_query'):
        high_score_terms = self.find_high_scores(term, mode, intent)
        if high_score_terms == 'Error001':
            return 'Error001'

        # get the uri for the terms
        uris = []
        for term in high_score_terms:
            score = term[1]
            term = term[0]
            try:
                uri = self.wiki_dictionary[mode]['dict'][term]
                print(uri)
                for u in uri:
                    uris.append((u.replace('http://www.wikidata.org/entity/', ''), score))
            except:
                pass
        return uris

    # three op
    def find_high_scores(self, term, mode='entity', intent='item_attribute_query'):
        dictionary = self.wiki_dictionary[mode]['dict']
        table = self.wiki_dictionary[mode]['list']

        # make a list of tuples including the term and the score
        temp = {}

        for word in table:
            temp[word] = fuzz.ratio(term.lower(), word.lower())

        t2 = temp

        # sort the list by the score, select top five self.top_k
        sort_orders = sorted(temp.items(), key=lambda x: x[1], reverse=True)
        temp_2 = sorted(t2.items(), key=lambda x: x[1], reverse=True)
        selected_terms = sort_orders[:self.top_k]
        highest_score = selected_terms[0][1]
        other_max_score = self.compare_other_dictionaries(term, intent=intent, mode=mode)
        if highest_score < other_max_score:
            # we have a problem of wrong intent recognition
            return 'Error001'

        print('============= todo =============')
        pprint(selected_terms)
        print('--------------------------------')

        return selected_terms

        # fuzz.ratio(term.lower(), Str2.lower())

    def parse_entities(self, entities):
        # {'entities': {'attribute': 'molecular weight', 'entity': 'benzene'},
        #  'type': 'item_attribute_query'}
        print('========== entities ============')
        print(entities)
        question_type = entities['type']
        list_of_entities = entities['entities']
        print(list_of_entities)
        results = []
        for key, value in list_of_entities.items():
            if key == 'comparison' or key == 'numerical_value':
                value = filter_components(term_type=key, term=value)
                if value is None:
                    return None
                obj_temp = {key: value}
                results.append(obj_temp)
            else:
                if type(value) is list:
                    for v in value:
                        v = filter_components(term_type=key, term=v)
                        if v is None:
                            return None
                        uris = self.find_matches_from_wiki(term=v, mode=key, intent=question_type)
                        if uris == 'Error001':
                            return 'Error001'
                        obj_temp = {key: remove_duplicated(uris)}
                        results.append(obj_temp)
                else:
                    value = filter_components(term_type=key, term=value)
                    if value is None:
                        return None
                    uris = self.find_matches_from_wiki(term=value, mode=key,intent=question_type)
                    if uris == 'Error001':
                        return 'Error001'
                    obj_temp = {key: remove_duplicated(uris)}
                    results.append(obj_temp)

        return {'intent': question_type, 'entities': results}
