export PGPASSWORD='Biui1227..';
psql -U postgres -h wqs97.click -p 5432 < cutConnection.sql
psql -U postgres -h wqs97.click -p 5432 < CreateSchema.sql
psql -U postgres -h wqs97.click -p 5432 < importData.sql