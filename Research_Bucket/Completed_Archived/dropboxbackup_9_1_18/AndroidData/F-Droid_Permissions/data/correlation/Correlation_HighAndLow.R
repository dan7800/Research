rm(list = ls())
#setwd("Desktop")


#source('XXXX.R')")


G1 = read.csv("data/Bottom10Percent.csv", header = T)
G2 = read.csv("data/Top10Percent.csv", header = T)


####	MWU Analysis



# OverprivCount


n = ncol(G1)
name_vec = names(G1)


i=9   # Only look at this row
#while(i <= n)
#{
  l = wilcox.test(G1[,"OverprivCount"], G2[,"OverprivCount"], alternative="less") ## Change col to be i if you want to cycle


  ### Just show all

    g = wilcox.test(G1[,"OverprivCount"], G2[,"OverprivCount"], alternative="greater")
    print(paste(name_vec["OverprivCount"], "is less with p value", l$p.value))
    print(paste(name_vec["OverprivCount"], "is greater with p value", g$p.value))

#  i = i + 1
#}

