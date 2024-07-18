import * as React from 'react'

import { getSpeciesOne, getSpeciesXYZ } from '@/lib/api/ontospecies'
import { capitalize } from '@/lib/utils'
import { Main } from '@/components/ui/main'
import { MolViewer } from '@/components/ui/mol-viewer'
import { PropertyTable } from './_components/property-table'

interface SpeciesPageInterface {
  params: { iriEncoded: string }
}

export default async function SpeciesPage({ params }: SpeciesPageInterface) {
  const [data, xyz] = await Promise.all([
    getSpeciesOne(params.iriEncoded),
    getSpeciesXYZ(params.iriEncoded),
  ])

  const identifiers = [
    ...[['The World Avatar IRI', [data.IRI]] as [string, string[]]],
    ...Object.entries(data.identifiers)
      .filter(([_, lst]) => lst.length > 0)
      .map(
        ([key, lst]) =>
          [
            key,
            lst
              .map(x => x.value)
              .filter(
                (value, index, array) => array.indexOf(value) === index
              ),
          ] as [string, string[]]
      ),
  ]

  return (
    <Main className='flex flex-col items-center'>
      <div className='w-full mt-8 px-4 flex flex-col space-y-2 md:max-w-screen-md lg:max-w-screen-lg'>
        <h1>{data.IUPAC_name ? capitalize(data.IUPAC_name) : data.InChI}</h1>
        <section>
          <MolViewer type='xyz' data={xyz} />
        </section>
        <section>
          <h2 id='identifiers'>
            <a href='#identifiers' className='hover:underline'>
              Identifiers
            </a>
          </h2>
          {identifiers.map(([key, lst], i) => (
            <div key={i} className='mb-2'>
              <h3 className='font-semibold'>{key}</h3>
              <ul>
                {lst.map((x, j) => (
                  <li key={j}>{x}</li>
                ))}
              </ul>
            </div>
          ))}
        </section>
        <section>
          <h2 id='chemical-classes'>
            <a href='#chemical-classes' className='hover:underline'>
              Chemical classifications
            </a>
          </h2>
          <ul className='list-disc list-inside'>
            {data.chemical_classes.map((node, i) => (
              <li key={i}>{node.label}</li>
            ))}
          </ul>
        </section>
        <section>
          <h2 id='uses'>
            <a href='#uses' className='hover:underline'>
              Uses
            </a>
          </h2>
          <ul className='list-disc list-inside'>
            {data.uses.map((node, i) => (
              <li key={i}>{node.label}</li>
            ))}
          </ul>
        </section>
        <section>
          <h2 id='properties'>
            <a href='#properties' className='hover:underline'>
              Properties
            </a>
          </h2>
          {Object.entries(data.properties)
            .filter(([_, lst]) => lst.length > 0)
            .map(([key, lst], i) => (
              <div key={i} className='mb-2'>
                <h3 id={`properties-${key}`} className='font-semibold'>
                  <a href={`#properties-${key}`} className='hover:underline'>
                    {key}
                  </a>
                </h3>
                <React.Suspense>
                  <PropertyTable rows={lst} />
                </React.Suspense>
              </div>
            ))}
        </section>
      </div>
    </Main>
  )
}
