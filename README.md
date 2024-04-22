# Touchstone-plus

Touchstone-plus is a supplementary version of Touchstone designed to address the insufficient support for matching operators.

## Technical Report

[Here](./technical-report.pdf) is our technical report, which is a extention of our submitted paper.
1. In Section 2, we give the proof for Proposation 1.
2. In Section 4, we give the proof for Theorem 1.

## Quick Start
 Touchstone-plus's workflow is divided into two steps: computation and data generation, which can be executed directly using the given command line.

### Computation
 The main task of computation is to extract table column information related to the input queries (including table names, column names, and cardinality of columns), as well as the cardinality of each query. Then, 
 based on this information, a Constraint Programming (CP) problem model is constructed, and the solver's results are output to a file.

 The configuration file path is ./conf/tool.json. Specifically, the configuration file tool.json is formatted to contain information such as database connection information and directory information.
1. `databaseConnectorConfig`: Database connection information. It is the connection configuration information for connecting to the target database.
2. `queriesDirectory`: The directory where the query is located refers to the query ready for simulation.
3. `resultDirectory`: The execution result storage directory refers to the directory where the parsed database information is stored.
4. `defaultSchemaName`: The default schema name of the database, this is due to the characteristics of Postgresql, you must specify the schema that belongs to, here fill in the default public is good.
### Data Generation
