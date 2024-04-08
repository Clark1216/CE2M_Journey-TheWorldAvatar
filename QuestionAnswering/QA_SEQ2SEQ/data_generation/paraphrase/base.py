from typing import List, Optional, Tuple
import Levenshtein
import numpy as np

from openai import OpenAI


class OpenAiClientForBulletPointResponse:
    PARAPHRASE_NUM = 3
    SYSTEM_PROMPT_TEMPLATE = '''You will be provided with a machine-generated query. Rephrase it in {num} different ways. Additionally, keep the square brackets, tags, and their enclosing text unchanged.
    
Text: """
{text}
"""

Paraphrases: 
'''

    def __init__(
        self,
        endpoint: None,
        api_key: Optional[str] = None,
        model: str = "gpt-4-0613",
        **kwargs: dict
    ):
        self.openai_client = OpenAI(base_url=endpoint, api_key=api_key)
        self.model = model
        self.kwargs = kwargs

    def _sanitize_bulletpoint(self, text: str):
        text = text.strip()

        if len(text) == 0:
            return text

        if text.startswith("-"):
            text = text[1:]
        elif text[0].isdigit():
            idx = 0
            while idx < len(text) and text[idx].isdigit():
                idx += 1
            text = text[idx:]
            text = text.strip()
            if text[0] in [".", ")"]:
                text = text[1:]

        text = text.strip()

        if text.startswith('"'):
            text = text[1:]
        if text.endswith('"'):
            text = text[:-1]

        return text

    def _is_bulleted_line(self, line: str):
        line = line.strip()
        return line.startswith("-") or (len(line) > 0 and line[0].isdigit())

    def _remove_opening_and_closing_statements(self, lines: List[str]):
        """Detects and removes opening and closing statements generated by OpenAI, if any.

        Example of an opening statement: "Certainly, here are 10 paraphrased versions of the statement:"

        Example of a closing statement: "Note: In the original statement, "enlist" is used, but it is
        unclear whether it refers to including them in a list or recruiting them for a specific purpose.
        Therefore, the paraphrases provided focus on the understanding that a list of molecules exhibiting
        specific characteristics is being requested."
        """
        if (
            len(lines) > 1
            and not self._is_bulleted_line(lines[0])
            and self._is_bulleted_line(lines[1])
        ):
            lines = lines[1:]

        if (
            len(lines) > 1
            and not self._is_bulleted_line(lines[-1])
            and self._is_bulleted_line(lines[-2])
        ):
            lines = lines[:-1]

        return lines

    def _parse_openai_response_content(self, content: str):
        lines = [x.strip() for x in content.split("\n")]
        lines = [x for x in lines if x]
        lines = self._remove_opening_and_closing_statements(lines)
        lines = [self._sanitize_bulletpoint(x) for x in lines]
        return lines

    def call(self, input_text: str):
        response = self.openai_client.chat.completions.create(
            model=self.model,
            messages=[
                {
                    "role": "user",
                    "content": self.SYSTEM_PROMPT_TEMPLATE.format(
                        num=self.PARAPHRASE_NUM, text=input_text
                    ),
                },
            ],
            **self.kwargs
        )

        response_content = response.choices[0].message.content
        return self._parse_openai_response_content(response_content)


class Paraphraser:
    TRY_LIMIT = 5
    FUZZY_TOLERANCE = 5

    def __init__(
        self,
        endpoint: Optional[str] = None,
        api_key: Optional[str] = None,
        model: str = "gpt-4-0613",
        openai_kwargs: Optional[dict] = None,
    ):
        if openai_kwargs is None:
            self.openai_client = OpenAiClientForBulletPointResponse(
                endpoint, api_key, model
            )
        else:
            self.openai_client = OpenAiClientForBulletPointResponse(
                endpoint, api_key, model, **openai_kwargs
            )

    def _extract_literals_by_tag(self, text: str):
        """Extracts literals enclosed by <span> tags."""
        literals: List[str] = []
        ptr = 0
        while ptr < len(text):
            start = text.find("<span>", ptr)
            if start < 0:
                break

            end = text.find("</span>", start)
            if end < 0:
                break

            ptr = end + len("</span>")
            literals.append(text[start:ptr])
        return literals

    def _extract_literals(self, text: str):
        # to be deprecated
        """Extracts literals enclosed by square brackets."""
        literals: List[str] = []
        ptr = 0
        while ptr < len(text):
            start = text.find("[", ptr)
            if start < 0:
                break

            # check that [ is not in the middle of a word
            if start > 0 and not text[start - 1].isspace():
                ptr = start + 1
                continue

            end = start + 1
            while end < len(text):
                end = text.find("]", end)
                if end < 0:
                    break
                if (
                    end + 1 == len(text)
                    or text[end + 1].isspace()
                    or text[end + 1] in ".,!?;"
                ):
                    break
                else:
                    end += 1

            if end < 0:
                break

            literals.append(text[start : end + 1])
            ptr = end + 1
        return tuple(literals)

    def _correct_paraphrase_by_tag(self, paraphrase: str, literals: Tuple[str, ...]):
        corrected = True

        for literal in literals:
            if literal in paraphrase:
                continue

            assert literal.startswith("<span>") and literal.endswith("</span>"), literal
            literal_core = literal[len("<span>") : -len("</span>")]
            literal_lower = literal_core.lower()
            w_num = len(literal_lower.split())

            if paraphrase.endswith(".") or paraphrase.endswith("?"):
                paraphrase = paraphrase[:-1]
            words = paraphrase.split()
            candidates = [words[i : i + w_num] for i in range(len(words) - w_num + 1)]
            if not candidates:
                corrected = False
                break

            dists = [
                Levenshtein.distance(" ".join(c).lower(), literal_lower)
                for c in candidates
            ]
            min_dist = min(dists)
            if min_dist > self.FUZZY_TOLERANCE:
                corrected = False
                break

            idxes = [i for i, d in enumerate(dists) if d == min_dist]
            if len(idxes) > 1:
                shortlisted = [candidates[i] for i in idxes]
                dists = [
                    Levenshtein.distance(" ".join(c), literal_core) for c in shortlisted
                ]
                idx = idxes[np.argmin(dists)]
            else:
                idx = idxes[0]

            paraphrase = "{pre} {mid} {post}".format(
                pre=" ".join(words[:idx]),
                mid=literal,
                post=" ".join(words[idx + w_num :]),
            )

        if corrected:
            return paraphrase
        else:
            return None

    def _correct_paraphrase(self, paraphrase: str, literals: Tuple[str, ...]):
        # to be deprecated
        corrected = True

        for l in literals:
            if l in paraphrase:
                continue

            _l = l
            if _l.startswith("["):
                _l = _l[1:]
            if _l.endswith("]"):
                _l = _l[:-1]
            _l = _l.strip()
            _l = _l.lower()
            w_num = len(_l.split())

            words = paraphrase.split()
            candidates = [words[i : i + w_num] for i in range(len(words) - w_num)]
            if not candidates:
                corrected = False
                break

            dists = np.array(
                [Levenshtein.distance(" ".join(c).lower(), _l) for c in candidates]
            )
            idx = np.argmin(dists)
            if dists[idx] > self.FUZZY_TOLERANCE:
                corrected = False
                break
            paraphrase = "{pre} {mid} {post}".format(
                pre=" ".join(words[:idx]), mid=l, post=" ".join(words[idx + w_num :])
            )

        if corrected:
            return paraphrase
        else:
            return None

    def paraphrase(self, text: str):
        # literals = self._extract_literals(text)
        literals = self._extract_literals_by_tag(text)

        try_num = 0
        paraphrases: List[str] = []
        rejected: List[str] = []
        while len(paraphrases) < 3 and try_num < self.TRY_LIMIT:
            for p in self.openai_client.call(text):
                if all(l in p for l in literals):
                    paraphrases.append(p)
                else:
                    # corrected = self._correct_paraphrase(p, literals)
                    corrected = self._correct_paraphrase_by_tag(p, literals)
                    if corrected:
                        print(
                            "Successful correction.\nFrom: {i}\nTo:{o}\n".format(
                                i=p, o=corrected
                            )
                        )
                        paraphrases.append(corrected)
                    else:
                        rejected.append(p)
            try_num += 1

        if len(paraphrases) < 3:
            print(
                "Unable to generate 3 faithful paraphrases.\nOriginal text: {og}\nParaphrases: {p}\nRejected: {rej}\n".format(
                    og=text, p=paraphrases, rej=rejected
                )
            )

        return paraphrases
