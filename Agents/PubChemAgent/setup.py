from setuptools import setup, find_packages

setup(
    name='pubchemagent',
    version='1.0.0',
    author='Ali Naseri, Laura Pascazio',
    license='MIT',
    long_description=open('README.md').read(),
    long_description_content_type="text/markdown",
    packages=find_packages(exclude=("tests")),
    url="",
    python_requires='>=3.5',
    include_package_data=True,
    install_requires= ['numpy','pyderivationagent','docopt','py4jps', 'flask'],
    entry_points={
        'console_scripts': [
            'pubchemagent=pubchemagent.driver:main',
        ],
    }
)