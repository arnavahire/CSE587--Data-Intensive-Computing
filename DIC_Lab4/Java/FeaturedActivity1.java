import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class FeaturedActivity1
{
	
	//-------------------------------------------CONSTRUCTOR-----------------------------------------------------//
	static Map<String, String> x;	
	
	public FeaturedActivity1()
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

		      for(int i=0;i<tokens.length;i++)         // emit word:location pair
		      {
		      	//Normalize the word
		      	
		      	String normalized_word=tokens[i].replace('j','i');
		      	normalized_word=normalized_word.replace('v','u');
		      	word.set(normalized_word);               
		      	if(!normalized_word.contains(" ")||normalized_word!=null)  // don't write a space or null value to reducer
		      	{
		      		context.write(word, new Text(location));
		      	}	
		      }       
	      }      
	  }
	}

	//----------------------------------------REDUCER CLASS-----------------------------------------------------//

	public static class IntSumReducer extends Reducer<Text,Text,Text,Text> 
	{
		
		public void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException 
		{
			String Finaloutput="";
			
			if(x.containsKey(key))
			{
				String[] lemmas=x.get(key).split(" ");
				for(int i=0;i<lemmas.length;i++)
				{
					for(Text val:values)
					{
						Finaloutput=Finaloutput+val.toString();
					}
					context.write(new Text(lemmas[i]),new Text(Finaloutput));
				}
			}
			else
			{
				for(Text val:values)
				{
					Finaloutput=Finaloutput+val.toString();		
				}
				context.write(new Text(key),new Text(Finaloutput));
			}
			
		}
	}
	
	//----------------------------------------MAIN METHOD--------------------------------------------------------//

	public static void main(String[] args) throws Exception 
	{
		    FeaturedActivity1 FA=new FeaturedActivity1(); 
		    Configuration conf=new Configuration();
		    Job job = Job.getInstance(conf, "FA1");
		    job.setJarByClass(FeaturedActivity1.class);
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