# > source('C:/Users/dkrutz/Desktop/Correlations.R')

rm(list = ls())

G1 = read.csv("C:/Users/dkrutz/Desktop/data/L10K.csv", header = T)
G2 = read.csv("C:/Users/dkrutz/Desktop/data/G10K.csv", header = T)

i = 2 ## Starting column
n = ncol(G1)
name_vec = names(G1)


### These are for normalizing the values.
#G1[,5:7] = 1000*(G1[,5:7]/G1[,6])
#G1[,9:n] = 1000*(G1[,9:n]/G1[,6])
#G2[,5:7] = 1000*(G2[,5:7]/G2[,6])
#G2[,9:n] = 1000*(G2[,9:n]/G2[,6]) 

while(i <= n)
{
  l = wilcox.test(G1[,i], G2[,i], alternative="less")
  if(l$p.value < 0.05)
    print(paste(name_vec[i], "is less with p value", l$p.value))
  g = wilcox.test(G1[,i], G2[,i], alternative="greater")
  if(g$p.value < 0.05)
    print(paste(name_vec[i], "is greater with p value", g$p.value))
 if(l$p.value < 0.05 | g$p.value < 0.05)
  {
 #   boxplot(G1[,i], G2[,i])
    #print(paste(name_vec[i], "Malware : ", median(G1[,i]),"avg:", mean(G1[,i])))
    #print(paste(name_vec[i], "GP : ",  median(G2[,i]),"avg:", mean(G2[,i])))


    print(paste(name_vec[i], "Malware : ",  median(G1[,i]),"avg:", round(mean(G1[,i]), digits = 3)))
    print(paste(name_vec[i], "GP : ",  median(G2[,i]),"avg:", round(mean(G2[,i]), digits = 3)))

  }
  i = i + 1
}
