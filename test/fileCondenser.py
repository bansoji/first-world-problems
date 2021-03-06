#This script takes a large input file and appends 100 (limit) lines of each company to the output file.

f = open("/Users/jasonlim/Desktop/multipleCompanies.csv", 'r')    #Input file.
g = open("/Users/jasonlim/Desktop/72Companies20Lines.csv", "w")   #Output file.
companies = {}
limit = 100

f.readline()                        #Skips the first line of the file (header).
for line in f:                                   
   fileLine = line.split(",")
   name = fileLine[0]
   if name not in companies:
      companies[name] = 0
   else:
      if companies[name] < limit:
         g.write(line)
         companies[name] += 1