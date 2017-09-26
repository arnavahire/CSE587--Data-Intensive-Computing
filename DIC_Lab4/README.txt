Activity1: 

1. The file 'DIC_Lab4_Notebook1.ipynb' is the R-notebook where I collect tweets.
2. Then I store the collected output in 'DIC4_1.txt'.
3. I pass this as an input to hadoop mapreduce.
4. The output I received was 'mapOutput.txt' which contains hashtags and their counts.
5. This output is again read in 'DIC_Lab4_Notebook1.ipynb' whcih I use to create wordCloud.
6. The output is present in the 'Activity1_wordcloud' folder in 'Outputs' folder.

Activity2:

1. The file 'DIC_Lab4_Notebook2.ipynb' is the R-notebook where I coolect tweets.
2. I pre-process the tweets and then the processed tweet's text is collected in 'Activity2.txt'.

Pairs:
3. I read this 'Activity2.txt' as input to hadoop.
4. The output of this is 'Activity2_Pairs_output' present in 'Outputs' folder.

Stripes:
3. I read this 'Activity2.txt' as input to hadoop.
4. The output of this is 'Activity2_Stripes_output' present in 'Outputs' folder.

FeaturedActivity1:

1. The contents in the folder 'FeaturedActivity1' in 'Inputs' folder are the inputs to hadoop.
2. The output is present in 'FeaturedActivity1_output' present in 'Outputs' folder.

FeaturedActivity2:

Part a:

1. The contents in the folder 'FeaturedActivity2' in 'Inputs' folder are the inputs to hadoop.	
2. Each folder (2latindocs, 4latindocs, 6latindocs, 8latindocs, 10latindocs) is given as an input for a single run of mapreduce. 
3. The output is present in 'FeaturedActivity2a_output' present in 'Outputs' folder. 
4. For plot there is 'Book 1.xlsx' file that is used for reference.
 
Part b:

1. The contents in the folder 'FeaturedActivity2' in 'Inputs' folder are the inputs to hadoop.
2. Each folder (2latindocs, 4latindocs, 6latindocs, 8latindocs, 10latindocs) is given as an input for a single run of mapreduce.
3. The output is present in 'FeaturedActivity2b_output' present in 'Outputs' folder. 
4. For plot there is 'Book 1.xlsx' file that is used for reference.

Refer 'readme.pdf' for more instructions.
