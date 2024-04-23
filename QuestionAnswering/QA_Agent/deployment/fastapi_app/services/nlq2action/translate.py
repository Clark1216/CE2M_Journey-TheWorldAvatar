import json
import os
from typing import Annotated
from fastapi import Depends
from openai import OpenAI
from .retrieve import Nlq2ActionRetriever, get_nlq2action_retriever


class Nlq2ActionTranslator:
    SYSTEM_MSG = "You are a SPARQL expert designed to output JSON."
    PROMPT_TEMPLATE = """### Examples of translating natural language questions to executable actions:
{examples}

### Instruction: 
Your task is to translate the following question to an executable action. Please do not provide any explanation and respond with a single JSON object exactly.

### Question:
{question}"""

    def __init__(
        self,
        retriever: Nlq2ActionRetriever,
        openai_base_url: str,
        openai_api_key: str,
        openai_model: str,
    ):
        self.retriever = retriever
        self.openai_client = OpenAI(base_url=openai_base_url, api_key=openai_api_key)
        self.model = openai_model

    def translate(self, nlq: str) -> dict:
        examples = self.retriever.retrieve_examples(nlq, k=10)
        prompt = self.PROMPT_TEMPLATE.format(
            examples="\n".join(
                '"{input}" => {output}'.format(
                    input=example.nlq, output=json.dumps(example.action)
                )
                for example in examples
            ),
            question='"{input}" => '.format(input=nlq),
        )
        res = self.openai_client.chat.completions.create(
            model=self.model,
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": self.SYSTEM_MSG},
                {"role": "user", "content": prompt},
            ],
        )
        return json.loads(res.choices[0].message.content)


def get_nlq2action_translator(
    retriever: Annotated[Nlq2ActionRetriever, Depends(get_nlq2action_retriever)]
):
    return Nlq2ActionTranslator(
        retriever=retriever,
        openai_base_url=os.getenv("TRANSLATOR_OPENAI_BASE_URL"),
        openai_api_key=os.getenv("TRANSLATOR_OPENAI_API_KEY"),
        openai_model=os.getenv("TRANSLATOR_OPENAI_MODEL"),
    )
