import argparse
import json
import os
from pathlib import Path
from typing import List

import openai
from tqdm import tqdm


def sanitize_bulletpoint(text: str):
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
        if text[0] == ".":
            text = text[1:]

    text = text.strip()

    if text.startswith('"'):
        text = text[1:]
    if text.endswith('"'):
        text = text[:-1]

    return text


def is_bulleted_line(line: str):
    return line.startswith("-") or (len(line) > 0 and line[0].isdigit())


def remove_opening_and_closing_statements(lines: List[str]):
    """Detects and removes opening and closing statements generated by OpenAI, if any.

    Example of an opening statement: "Certainly, here are 10 paraphrased versions of the statement:"

    Example of a closing statement: "Note: In the original statement, "enlist" is used, but it is 
    unclear whether it refers to including them in a list or recruiting them for a specific purpose. 
    Therefore, the paraphrases provided focus on the understanding that a list of molecules exhibiting 
    specific characteristics is being requested."
    """
    if len(lines) > 1 and not is_bulleted_line(lines[0].strip()) and is_bulleted_line(lines[1].strip()):
        lines = lines[1:]
    
    if len(lines) > 1 and not is_bulleted_line(lines[-1].strip()) and is_bulleted_line(lines[-2].strip()):
        lines = lines[:-1]

    return lines


def parse_openai_paraphrase_response_content(content: str):
    lines = [x.strip() for x in content.split("\n")]
    lines = [x for x in lines if x]
    lines = remove_opening_and_closing_statements(lines)
    lines = [sanitize_bulletpoint(x) for x in lines]
    return lines


def get_paraphrases(text: str):
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
            {
                "role": "user",
                "content": "Paraphrase the following statement in 10 different ways.\n"
                + text,
            },
        ],
    )

    response_content = response["choices"][0]["message"]["content"]
    paraphrases = parse_openai_paraphrase_response_content(response_content)

    if len(paraphrases) != 10:
        print("Unexpected OpenAI's response format of paraphrases encountered.")
        print("Text: ", text)
        print("Response content: \n", response_content)

    return paraphrases


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_path", type=str)
    parser.add_argument("output_path", type=str)
    parser.add_argument("--start_index", type=int, default=0)
    args = parser.parse_args()

    with open(args.input_path, "r") as f:
        data = json.load(f)

    Path(args.output_path).mkdir(parents=True, exist_ok=True)

    batch_size = 10
    data_out = []
    for i, datum in enumerate(tqdm(data)):
        if i < args.start_index:
            continue

        paraphrases = get_paraphrases(datum["question"])
        data_out.append(dict(question=datum["question"], paraphrases=paraphrases))

        if (i + 1) % batch_size == 0 or i == len(data) - 1:
            filename = str(i // batch_size * batch_size) + ".json"
            filepath = os.path.join(args.output_path, filename)
            with open(filepath, "w") as f:
                json.dump(data_out, f, indent=4)
            data_out = []
