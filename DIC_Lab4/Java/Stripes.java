import java.io.IOException;
import java.util.StringTokenizer;
import java.util.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Stripes {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, PrintMapWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private PrintMapWritable map;
    private Text temp=new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());  // Contains the sentence
      String[] tokens=new String[itr.countTokens()];

      for(int i=0;i<tokens.length;i++) {
        tokens[i]=itr.nextToken();               // Values are stored in the token array now
      }

      for(int i=0;i<tokens.length;i++)         // emit pairs one by one
      {
      	map=new PrintMapWritable();
      	word.set(tokens[i]);                // new map needs to be created for each word
        for(int j=0;j<tokens.length;j++)
        {
        	if(j==i)                      // if same word appears as neighbor then ignore it
        	{
        		continue;
        	}
          
          temp.set(tokens[i]+" "+tokens[j]);       // add 'word:neighbour' as key in map and value as count=1
          map.put(temp,one);
          context.write(word,map);                // emit each map for a word
        }
                 // emit the key i.e the word and its hashmap as the output of mapper
          
      }
    }
  }

  public static class PrintMapWritable extends MapWritable{
  	
  	public String toString()
  	{
  		StringBuilder res=new StringBuilder();
  		Set<Writable> keyS=this.keySet();
  		for(Object k:keyS)             
  		{
  			res.append("{"+k.toString()+"="+this.get(k)+"}");
  		}
  		return res.toString();
  	}
  }

  public static class IntSumReducer
       extends Reducer<Text,PrintMapWritable,Text,PrintMapWritable> {

    public void reduce(Text key, Iterable<PrintMapWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
    PrintMapWritable tempMap=new PrintMapWritable();  
      for (PrintMapWritable val : values) {                // for every map received
      	Set<Writable> set_of_keys=val.keySet();       // extract set of keys in the map
      	for(Writable key1:set_of_keys){                           // for every key in the keyset
      		
      		IntWritable mapCount=(IntWritable) val.get(key1);      // get count of that key
      		if(tempMap.containsKey(key1))                          // if the tempMap contains the key, get the count of that key and append the new key's count to that
      		{
      			IntWritable tempCount=(IntWritable) tempMap.get(key1);
      			int a=tempCount.get();                                     
      			a++;             
      			tempCount.set(a);                  // increment the count for the already present key
      			tempMap.put(key1,tempCount);

      		}
      		else                                                         // if the tempMap doesnt contain that key, add that key to it
      		{
      			tempMap.put(key1,mapCount);
      		}
      	}
        
     }
      context.write(key, tempMap);           // emit the word, and its map as the output
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "stripes");
    job.setJarByClass(Stripes.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(PrintMapWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}