"use client";

import React, { MouseEvent, useState } from "react";
import Image from "next/image";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { DataItem } from "@/lib/model";
import { queryQa } from "@/lib/api";
import { DataTable } from "@/components/ui/data-table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input"
import { MagnifyingGlassIcon } from "@radix-ui/react-icons";

export default function Home() {
  const [question, setQuestion] = useState("");
  const [qaData, setQaData] = useState<DataItem[] | null>(null);

  const handleSubmit = async (e: MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    const qaRes = await queryQa(question);
    setQaData(qaRes.data);
  }

  return (
    <main className="p-4 min-h-screen flex flex-col justify-center items-center">
      <div className="max-w-4xl mb-8 grid gap-4 lg:grid-cols-4">
        <div className="grid place-content-center">
          <Image
            src="/images/marie-thumbnail.jpg"
            alt="Marie thumbnail"
            width={175}
            height={250}
            priority
          />
        </div>
        <div className="text-sm content-center space-y-4 lg:col-span-3">
          <h1 className="text-3xl font-semibold text-blue-500">Marie</h1>
          <p>
            This website presents a proof-of-concept search engine system for
            accessing chemical data from the World Avatar Knowledge Graph. The
            Knowledge Graphs offers inter-connected data from chemical kinetics
            to chemical and physical properties of species and many other
            domains.
          </p>
          <p>
            Marie offers multiple interaction modes to cater to different user
            needs:
          </p>
          <ul className="list-disc list-inside">
            <li>
              Type a question into the field below to use the search engine or
              select one of the provided sample questions.
            </li>
            <li>
              Utilize the Advanced Search tab to conduct detailed searches by
              defining filters and ranges for specific properties. This mode
              provides a more targeted approach to data retrieval.
            </li>
            <li>
              The Explore tab enables users to visually explore and plot
              properties of chemical species and zeolites, offering a graphical
              representation of data for enhanced analysis.
            </li>
          </ul>
          <p>
            For those interested in the development history of Marie and related
            research, the History tab provides access to relevant information
            and publications. Please note that Marie is still under development
            and the accuracy of the results will increase with further
            refinement of the underlying ontologies. We welcome feedback and
            suggestions as we work to refine and expand its capabilities.
          </p>
        </div>
      </div>
      <div className="max-w-3xl mb-12 flex justify-center items-center w-full">
        <Input placeholder="Type your query..." className="mr-2" onChange={e => setQuestion(e.target.value)} />
        <Button variant="outline" size="icon" onClick={handleSubmit}><MagnifyingGlassIcon /></Button>
      </div>
      <div className="max-w-4xl">
        {qaData && qaData.map((item, idx) => item.type === "table" ? <DataTable key={idx} headers={item.columns} data={item.data} /> : <></>)}
      </div>
      <div className="max-w-2xl w-full">
        <h2>Example Questions</h2>
        <Accordion type="single" collapsible className="w-full">
          <AccordionItem value="item-1">
            <AccordionTrigger>Chemical Species Properties</AccordionTrigger>
            <AccordionContent>
              <ul>
                <li>What is the charge of benzene?</li>
                <li>Provide a roster of chemical entities used as pH regulating agent</li>
              </ul>
            </AccordionContent>
          </AccordionItem>

          <AccordionItem value="item-2">
            <AccordionTrigger>Gas-Phase Reaction Mechanisms</AccordionTrigger>
            <AccordionContent>
              <ul>
                <li>List all mechanisms that involve O, H, and AR</li>
                <li>What are reactions in which O2 is a reactant and OH is a product</li>
              </ul>
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      </div>
    </main>
  );
}
