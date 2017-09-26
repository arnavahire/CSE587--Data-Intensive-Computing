import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class LatinCoccurence2 {

	//-------------------------------------------CONSTRUCTOR-----------------------------------------------------//
			static Map<String, String> x;	
			
			public LatinCoccurence2()
			{
				this.x=new HashMap<String, String>();
				
				try
				{
					String filepath="/home/hadoop/new_lemmatizer.csv";
					BufferedReader br=new BufferedReader(new FileReader(filepath));
					String line="";
					String cvsSplitBy=",";
					while((line=br.readLine())!=null)
					{
						String[] splits=line.split(cvsSplitBy);
						String key=splits[0];  // Word 
						for(int i=0;i<splits.length;i++)
						{
							if(i==0)
							{
								continue;
							}
							if(x.containsKey(key))
							{
								String str=x.get(key);       // get previous value
								String newstr=str+" "+splits[i];   // append new value to it
								x.put(key,newstr);                  // hashmap of words=keys and list of lemmas sepearated by space=values
							}
							else
							{
								String str=splits[i];
								x.put(key, str);     //   if no key present, put the corresponding key and value
							}
						}				                   
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		//---------------------------------------MAPPER CLASS--------------------------------------------------------//
			
			
			public static class TokenizerMapper extends Mapper<Object, Text, Text, Text>
			{

				  private Text word = new Text();

				  public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
				  {
					  StringTokenizer itr = new StringTokenizer(value.toString(),">");  // Contains the sentence ex: <luc. 1.1>	Bella per Emathios plus quam civilia campos
					 	  
				      String[] twotokens=new String[itr.countTokens()];                  //<luc. 1.1 ||  	Bella per Emathios plus quam civilia campos    
				      
				      if(twotokens.length>1)
				      {
				    	  for(int i=0;i<twotokens.length;i++)
					      {
					    	  twotokens[i]=itr.nextToken();
					      }
				    	  String location=twotokens[0]+">";                      		 // extract location by adding '>'. ex: <luc. 1.1>
				    	  String final_line=twotokens[1].trim();      //trim() removes any preceding tab or space
					      String[] tokens=final_line.split(" ");        // Bella || per || Emathios || plus || quam || civilia campos 
					                        // we get the sentence here. We split it to get individual words ex: Bella per Emathios plus quam civilia campos               

					      for(int i=0;i<tokens.length-2;i++)         // emit word:location pair
					      {
					      	//Normalize the word
					      	
					      	String normalized_word=tokens[i].replace('j','i');
					      	normalized_word=normalized_word.replace('v','u'); // normalize the word to be searched   
					      	for(int j=i+1;j<tokens.length-1;j++)
					      	{
				
					      		String normalized_word2=tokens[j].replace('j','i');  // normalize neighbour1
						      	normalized_word2=normalized_word2.replace('v','u');
						      	/*word.set(normalized_word+","+normalized_word2);
						      	context.write(word,new Text(location));    */       // emit key=(word,neighbour) and value=(location)
						      	for(int k=j+1;k<tokens.length;k++)
						      	{
						      		String normalized_word3=tokens[j].replace('j','i');  // normalize neighbour2
							      	normalized_word3=normalized_word3.replace('v','u');
							      	word.set(normalized_word+","+normalized_word2+","+normalized_word3);
							      	context.write(word,new Text(location));
						      	}
						      	
					      	}			      	
					      }       
				      }      
				  }
			}

	
			//--------------------------------------------REDUCER CLASS-------------------------------------------------------//

			public static class IntSumReducer extends Reducer<Text,Text,Text,Text> 
			{
				
				public void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException 
				{
					String Finaloutput="";
					
					String[] fragments=key.toString().split(",");  // word||neighbor
					if(fragments.length>2)
					{
						String word=fragments[0];
						String neighbor1=fragments[1];
						String neighbor2=fragments[2];
						if(x.containsKey(word))
						{
							String[] lemmas=x.get(word).split(" ");
							for(int i=0;i<lemmas.length;i++)
							{
								if(x.containsKey(neighbor1))       // if neighbor1 present in hashmap
								{
									String[] N1lemmas=x.get(neighbor1).split(" ");
									for(int j=0;j<N1lemmas.length;j++)
									{
										if(x.containsKey(neighbor2))       // if neighbor2 present in hashmap
										{
											String[] N2lemmas=x.get(neighbor2).split(" ");
											for(int k=0;k<N2lemmas.length;k++)
											{
												for(Text val:values)
												{
													Finaloutput=Finaloutput+val.toString();
												}
												context.write(new Text(lemmas[i]+","+N1lemmas[j]+","+N2lemmas[k]), new Text(Finaloutput));
											}
										}
										else                         // if neighbor2 not present
										{
											for(Text val:values)
											{
												Finaloutput=Finaloutput+val.toString();		
											}
											context.write(new Text(lemmas[i]+","+N1lemmas[j]+","+neighbor2),new Text(Finaloutput));
										}
										
									
									}
								}               // if neighbor1 is absent
								else
								{
									if(x.containsKey(neighbor2))
									{
										String[] N2lemmas=x.get(neighbor2).split(" ");
										for(int k=0;k<N2lemmas.length;k++)
										{
											for(Text val:values)
											{
												Finaloutput=Finaloutput+val.toString();
											}
											context.write(new Text(lemmas[i]+","+neighbor1+","+N2lemmas[k]), new Text(Finaloutput));
										}
									}
								}							
							}
						}
						else             // if word is absent
						{
							for(Text val:values)
							{
								Finaloutput=Finaloutput+val.toString();		
							}
							context.write(new Text(key),new Text(Finaloutput));
						}
					}
					
					
					
					
				}
			}	
		
	
	//----------------------------------------MAIN METHOD--------------------------------------------------------//

		public static void main(String[] args) throws Exception 
		{
				LatinCoccurence2 lc=new LatinCoccurence2();
			    Configuration conf=new Configuration();
			    Job job = Job.getInstance(conf, "FA2");
			    job.setJarByClass(LatinCoccurence2.class);
			    job.setMapperClass(TokenizerMapper.class);
			    job.setCombinerClass(IntSumReducer.class);
			    job.setReducerClass(IntSumReducer.class);
			    job.setOutputKeyClass(Text.class);
			    job.setOutputValueClass(Text.class);
			    FileInputFormat.addInputPath(job, new Path(args[0]));
			    FileOutputFormat.setOutputPath(job, new Path(args[1]));
			    System.exit(job.waitForCompletion(true) ? 0 : 1);
		}
}
