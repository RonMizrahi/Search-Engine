FOR /L %%A IN (1,1,10) DO (
  start java -jar client.jar %%A.txt
)