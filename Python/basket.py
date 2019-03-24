import numpy as np
from mlxtend.frequent_patterns import apriori
from mlxtend.frequent_patterns import association_rules
import pandas as pd
import sys
from firebase import firebase

#Firebase connection Just 1 line baby 
firebase = firebase.FirebaseApplication('https://marketbasketanalyser-ba6b6.firebaseio.com/')

minsupport = 0.07

country = ""
#start_date = ""
#end_date = ""
q_num =""
quarter = ""

a = []
for arg in sys.argv:
    a.append(arg)
"""if len(a) == 4:
    country = a[1]
    start_date = a[2]
    end_date = a[3]"""

if len(a) == 3:
    q_num = a[1]
    quarter = a[2]
    #start_date = a[1]
    #end_date = a[2] 
    
if len(a) == 2:
    country = a[1]
  

df = pd.read_excel('data.xlsx')

"""if ((start_date != "") and (end_date != "")) :
    df['InvoiceDate'] = pd.to_datetime(df['InvoiceDate'])
    mask = (df['InvoiceDate'] > start_date) & (df['InvoiceDate'] <= end_date)
    df = df.loc[mask]"""

if((q_num == 'q')and (quarter != "")):
    if(quarter == "one"):
        mask = (df.InvoiceDate.dt.month >= 1 ) & (df.InvoiceDate.dt.month < 5)
        df = df.loc[mask]
    if(quarter == "two"):
        mask = (df.InvoiceDate.dt.month >= 5 ) & (df.InvoiceDate.dt.month < 9)
        df = df.loc[mask]
    if(quarter == "three"):
        mask = (df.InvoiceDate.dt.month >= 9 ) & (df.InvoiceDate.dt.month < 13)
        df = df.loc[mask]


    

df['Description'] = df['Description'].str.strip()
df.dropna(axis=0,subset=['InvoiceNo'],inplace=True)
df['InvoiceNo'] = df['InvoiceNo'].astype('str')
df = df[~df['InvoiceNo'].str.contains('C')]

if len(country)==0:
    basket = (df.groupby(['InvoiceNo', 'Description'])['Quantity'].sum().unstack().reset_index().fillna(0).set_index('InvoiceNo'))
    #minsupport = 0.02
if len(country)!= 0:
    basket = (df[df['Country'] ==country].groupby(['InvoiceNo', 'Description'])['Quantity'].sum().unstack().reset_index().fillna(0).set_index('InvoiceNo'))

if len(basket)>= 14000 :
    minsupport = 0.02
elif len(basket)>=4000:
    minsupport =0.02
elif len(basket) >= 450:
    minsupport = 0.05
elif len(basket) >= 50:
    minsupport = 0.1
elif len(basket) < 50 :
    minsupport = 0.1

print(len(basket))
print(minsupport)

def encode_units(x):
    if x <= 0:
        return 0
    if x >= 1:
        return 1

basket_sets = basket.applymap(encode_units)  
basket_sets.drop('POSTAGE', inplace=True, axis=1)
frequent_itemsets = apriori(basket_sets, min_support=minsupport, use_colnames=True)
rules = association_rules(frequent_itemsets, metric="lift", min_threshold=1)
rules2 = rules.sort_values('lift', ascending=False)
print("Antecedents" + "\t\t" + "Consequents" + "\t\t" + "Confidence" + "\t\t" + "Lift")
for index, row in rules2.head(15).iterrows():
       print (list(row['antecedents']), list(row['consequents']),row['confidence'],row['lift'])
       ant = list(row['antecedents'])
       con = list(row['consequents'])
       conf = round(row['confidence'],4) * 100
       lif = round(row['lift'],4)
       if quarter != "":
            result = firebase.post('/Quarter/'+quarter,{'antecedents':ant[0],'consequents':con[0],'confidence':str(conf),'lift':str(lif)})

    #if country != "":
        #result = firebase.post('/Country/'+country,{'antecedents':ant[0],'consequents':con[0],'confidence':str(conf),'lift':str(lif)})
        
            #else:
        #result = firebase.post('/Total/Country',{'antecedents':ant[0],'consequents':con[0],'confidence':str(conf),'lift':str(lif)})

