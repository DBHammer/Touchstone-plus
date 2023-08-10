DROP DATABASE IF EXISTS tpchonecolumn;
CREATE DATABASE tpchonecolumn;
\c tpchonecolumn;
CREATE TABLE public.part (
  P_TYPE CHAR(50) DEFAULT NULL
);

