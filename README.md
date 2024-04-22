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
2. `inputDirectory`: The directory where the query is located refers to the query ready for simulation.
3. `outputDirectory`: The directory for storing parsed results and solver computation results.
4. `newsqlDirectory`: The directory of simulated queries obtained during the generation phase.
5. `dataDirectory`: The directory of simulated data obtained during the generation phase.

An example is shown below.
```json lines
{
  "databaseConnectorConfig": {
    "databaseIp": "127.0.0.1", //database IP
    "databaseName": "tpch1", //database name
    "databasePort": "5432", //database port
    "databasePwd": "mima123", //database password
    "databaseUser": "postgres" //database username
  },
  "inputDirectory": "conf/inputTest.txt", //directory where the query is located
  "outputDirectory": "conf/output.txt", //execution result storage directory
  "newsqlDirectory": "conf/newsql.txt", //directory where the simulated query is located
  "dataDirectory": "conf/data.txt", //directory where the simulated query is located
}
```

The command for executing the computation phase task via the command line is
```bash
java -jar Mirage-${version}.jar solve -c conf/tool.json -t ${thred number} -e ${comoutation error allowed} -s ${scale error}
```

The specific parameters are shown below:
```shell
-t, --The number of threads used by the solver.
-e, --The maximum allowable error $\Epsilon$ (corresponding to optimization method 2).
-s, --The parameter $\rho$ for scaling the value range (corresponding to optimization method 2).
```
### Data Generation
