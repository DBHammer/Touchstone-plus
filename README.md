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
### Data Generation
