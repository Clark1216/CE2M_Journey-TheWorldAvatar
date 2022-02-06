from setuptools import setup, find_packages

setup(
    name='doeagent',
    version='0.0.1',
    author='Jiaru Bai',
    author_email='jb2197@cam.ac.uk',
    license='MIT',
    python_requires='>=3.7',
    description="doeagent is capable of conducting design of experiment exercise as part of The World Avatar project.",
    # url="https://github.com/cambridge-cares/TheWorldAvatar/tree/133-dev-design-of-experiment/Agents/DoEAgent",
    # long_description=open('README.md').read(),
    # long_description_content_type="text/markdown",
    packages=find_packages(exclude=('tests')),
    install_requires=['pyasyncagent==0.0.5', 'summit', 'pandas', 'pydantic'
    # 'agentlogging @ git+https://github.com/cambridge-cares/TheWorldAvatar@develop#subdirectory=Agents/utils/python-utils'
    ],
    extras_require={
        "dev": [
            "testcontainers>=3.4.2",
            "pytest>=6.2.3",
        ],
    },
    # include_package_data= True
)
