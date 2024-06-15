'use client'

import * as React from 'react'

import Markdown from 'react-markdown'

import { DataItem, QAResponse, QAResponseMetadata } from '@/lib/model'
import { cn, makePrefixedIRI } from '@/lib/utils'
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion'
import { DataTable } from '@/components/ui/data-table'
import { JSONTree } from '@/components/ui/json-tree'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { MolViewer } from '@/components/ui/mol-viewer'
import { StopIcon } from '@radix-ui/react-icons'

export interface QAResponseMetadataDivProps
  extends React.HTMLAttributes<HTMLDivElement> {
  qaResponseMetadata: QAResponseMetadata
}

export const QAResponseMetadataDiv = ({
  qaResponseMetadata,
  ...props
}: QAResponseMetadataDivProps) => (
  <div {...props}>
    <h2 className='text-xl font-semibold text-blue-500'>Processing steps</h2>
    <Accordion type='multiple'>
      {qaResponseMetadata.rewritten_question && (
        <AccordionItem value='rewritten_question'>
          <AccordionTrigger>Rewritten input question</AccordionTrigger>
          <AccordionContent>
            <p>The input query has been rewritten into the following:</p>
            <p>&quot;{qaResponseMetadata.rewritten_question}&quot;</p>
          </AccordionContent>
        </AccordionItem>
      )}
      <AccordionItem value='translation_context'>
        <AccordionTrigger>Translation context</AccordionTrigger>
        <AccordionContent className='px-4'>
          <Accordion type='multiple'>
            <AccordionItem value='schema_relations'>
              <AccordionTrigger>
                Relations retrieved from knowledge base with highest relevance
              </AccordionTrigger>
              <AccordionContent>
                <DataTable
                  columns={[
                    { value: 'iri', label: 'IRI' },
                    { value: 'label', label: 'Label' },
                    { value: 'comment', label: 'Comment' },
                  ]}
                  data={qaResponseMetadata.translation_context.properties.map(
                    obj =>
                    ({
                      iri: makePrefixedIRI(obj.iri),
                      label: obj.label,
                      comment: obj.comment
                    })
                  )}
                />
              </AccordionContent>
            </AccordionItem>
            <AccordionItem value='examples'>
              <AccordionTrigger>
                Semantic parsing examples retrieved from database with highest
                relevance
              </AccordionTrigger>
              <AccordionContent>
                <DataTable
                  columns={[
                    { value: 'nlq', label: 'Natural language question' },
                    { value: 'var2cls', label: 'Class assignment' },
                    { value: 'entity_bindings', label: 'Entity bindings' },
                    { value: 'req_form', label: 'Structured query form' },
                  ]}
                  data={qaResponseMetadata.translation_context.examples.map(
                    example => ({
                      nlq: example.nlq,
                      var2cls: (
                        <JSONTree data={example.data_req.var2cls} />
                      ),
                      entity_bindings: (
                        <JSONTree data={example.data_req.entity_bindings} />
                      ),
                      req_form: <JSONTree data={example.data_req.req_form} />,
                    })
                  )}
                />
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        </AccordionContent>
      </AccordionItem>
      <AccordionItem value='prediction'>
        <AccordionTrigger>Predicted structured query</AccordionTrigger>
        <AccordionContent className='px-4'>
          <Accordion type='multiple'>
            <AccordionItem value='class_assignment'>
              <AccordionTrigger>Class assignment</AccordionTrigger>
              <AccordionContent>
                <DataTable columns={[
                  { value: 'var', label: 'Variable' },
                  { value: 'cls', label: 'Class' }
                ]}
                  data={Object.entries(qaResponseMetadata.data_request.var2cls).map(([varname, cls]) => ({ var: varname, cls }))} />
              </AccordionContent>
            </AccordionItem>
            <AccordionItem value='entity_bindings'>
              <AccordionTrigger>
                Entity recognition and linking
              </AccordionTrigger>
              <AccordionContent>
                <DataTable
                  columns={[
                    { value: 'var', label: 'Variable' },
                    { value: 'cls', label: 'Class' },
                    { value: 'mention', label: 'Mention' },
                    { value: 'linked_iris', label: 'Linked IRIs' },
                  ]}
                  data={Object.entries(
                    qaResponseMetadata.data_request.entity_bindings
                  ).map(([varname, values]) => ({
                    var: varname,
                    cls: qaResponseMetadata.data_request.var2cls[varname],
                    mention: values.map(val =>
                      typeof val === 'string'
                        ? val
                        : Object.entries(val)
                          .map(([k, v]) => `${k}: ${v}`)
                          .join('\n')
                    ),
                    linked_iris: qaResponseMetadata.linked_variables[varname],
                  }))}
                />
              </AccordionContent>
            </AccordionItem>
            {
              qaResponseMetadata.data_request.req_form && (
                <AccordionItem value='data_req_form'>
                  <AccordionTrigger>Structured query form</AccordionTrigger>
                  <AccordionContent className='px-6'>
                    {qaResponseMetadata.data_request.req_form.type === 'sparql' ? (
                      <>
                        <h4 className='font-medium'>Namespace</h4>
                        <p className='mb-2'>
                          {qaResponseMetadata.data_request.req_form.namespace}
                        </p>
                        <h4 className='font-medium'>SPARQL query</h4>
                        <p className='font-mono whitespace-pre bg-slate-50 p-4'>
                          {qaResponseMetadata.data_request.req_form.query}
                        </p>
                      </>
                    ) : (
                      <></>
                    )}
                  </AccordionContent>
                </AccordionItem>
              )
            }
          </Accordion>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  </div>
)

export interface QAResponseDataDivProps
  extends React.HTMLAttributes<HTMLDivElement> {
  qaResponseData: DataItem[]
}

export const QAResponseDataDiv = ({
  qaResponseData,
  ...props
}: QAResponseDataDivProps) => (
  <div {...props}>
    <h2 className='text-xl font-semibold text-blue-500'>Retrieved data</h2>
    <Accordion
      type='multiple'
      defaultValue={qaResponseData.map((_, idx) => idx.toString())}
    >
      {qaResponseData.map((item, idx) => {
        let headerText, component
        if (item.type === 'document_collection') {
          headerText = 'JSON data'
          component = <JSONTree data={item.data} shouldExpandNodeInitially={() => false} />
        } else if (item.type === 'table') {
          headerText = 'Tabular data'
          component = <DataTable columns={item.columns} data={item.data} />
        }
        return headerText && component ? (
          <AccordionItem key={idx} value={idx.toString()}>
            <AccordionTrigger>{headerText}</AccordionTrigger>
            <AccordionContent className='py-2'>{component}</AccordionContent>
          </AccordionItem>
        ) : (
          <></>
        )
      })}
    </Accordion>
  </div>
)

export interface QAResponseDivProps
  extends React.HTMLAttributes<HTMLDivElement> {
  qaResponse?: QAResponse
  chatAbortController?: AbortController
  chatStream?: ReadableStreamDefaultReader<string>
}

export function QAResponseDiv({
  qaResponse,
  chatAbortController,
  chatStream,
  className,
  ...props
}: QAResponseDivProps) {
  const chatRef = React.useRef<null | HTMLDivElement>(null)
  const [isGeneratingChat, setIsGeneratingChat] = React.useState<boolean>(false)
  const [chatAnswer, setChatAnswer] = React.useState<string | undefined>(
    undefined
  )

  React.useEffect(() => {
    if (!chatStream) return

    const pump = ({
      done,
      value,
    }: {
      done: boolean
      value?: string
    }): Promise<void> => {
      if (done) {
        return Promise.resolve()
      }

      // TODO: Use TransformerStream to do the parsing in API call code
      if (value) {
        value.split('\n').forEach(line => {
          const trimmedLine = line.trim()
          if (trimmedLine.startsWith('data: ')) {
            const msg = trimmedLine.substring('data: '.length)
            try {
              const dataChunk = JSON.parse(msg)
              const content = dataChunk['content']
              if (typeof content === 'string') {
                setChatAnswer(oldValue => (oldValue || '') + content)
              }
            } catch (err) {
              console.log('Unexpected data received from server: '.concat(msg))
            }
          }
        })
      }
      return chatStream.read().then(pump)
    }

    const readStream = async () => {
      setIsGeneratingChat(true)
      try {
        await chatStream.read().then(pump)
      } catch (err) {
      } finally {
        setIsGeneratingChat(false)
      }
    }

    readStream()
  }, [chatStream])

  React.useEffect(() => {
    chatRef.current?.scrollIntoView(false)
  }, [chatAnswer])

  const handleAbort = () => {
    chatAbortController?.abort()
    setIsGeneratingChat(false)
  }

  return (
    <div className={cn('flex flex-col space-y-6', className)} {...props}>
      {qaResponse && (
        <>
          <QAResponseMetadataDiv qaResponseMetadata={qaResponse.metadata} />
          {(qaResponse.visualisation.length > 0) && (
            <div>
              <h2 className='text-xl font-semibold text-blue-500 mb-2'>
                Chemical Structure Visualisation
              </h2>
              <Tabs defaultValue='0' className='grid lg:grid-cols-4 gap-4'>
                <div>
                  <TabsList className='flex lg:flex-col space-y-1'>
                    {qaResponse.visualisation.map(({ label }, i) => (
                      <TabsTrigger key={i} value={i.toString()}>
                        {label}
                      </TabsTrigger>
                    ))}
                  </TabsList>
                </div>
                <div className='lg:col-span-3'>
                  {qaResponse.visualisation.map(({ type, data }, i) => (
                    <TabsContent key={i} value={i.toString()}>
                      <MolViewer type={type} data={data} />
                    </TabsContent>
                  ))}
                </div>
              </Tabs>
            </div>
          )}
          <QAResponseDataDiv qaResponseData={qaResponse.data} />
        </>
      )}
      {chatAnswer && (
        <div ref={chatRef}>
          <h2 className='text-xl font-semibold text-blue-500 mb-2'>
            Marie&apos;s response
          </h2>
          <Markdown className='prose max-w-none prose-sm prose-slate'>
            {chatAnswer}
          </Markdown>
          {isGeneratingChat && (
            <div
              onClick={handleAbort}
              className='mb-4 flex items-center justify-center space-x-2 hover:cursor-pointer'
            >
              <StopIcon className='h-4 w-4' />
              <p>Stop generating</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
