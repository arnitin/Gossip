import scala.actors.Actor
import scala.actors.Actor._
import scala.math
import scala.collection.mutable.ArrayBuffer
import scala.actors.TIMEOUT
import scala.collection.mutable._

object proj2 {

  def main (args:Array[String]) : Unit = {
  //val numNodes:Int = 3
  //val topology :String = "full"
   var numNodes = 9
   var flag = 1
   var dropCount = 10;
   //var topology:String = "line"
   //var topology:String = "2D"
   //var topology:String = "imp2D"
  var topology:String = "full"
   var algorithm = ""
   var gossipFlag = 0
   if(args.length >1){
     numNodes = args(0).toInt
     topology = args(1)
     algorithm = args(2)
     dropCount = args(3).toInt
    }
    
   if(gossipFlag == 1){
	   dropCount = -1
   }
   if(algorithm == "gossip") {
     gossipFlag = 1
   }
    var i:Int = 0;
    var j:Int = 0;
    var MDarray = Array.ofDim[Int](numNodes,numNodes);
        /////////////////////////////////println("num of nodes is "+ numNodes)
        if(topology == "full"){
           while(i<numNodes) {
             j = 0;
             while(j<numNodes){
                 MDarray(i)(j) = 1;
                 if (i == j){
                   MDarray(i)(j) = 0;
                 }
                 j = j + 1;
             }
             i = i + 1;
           }
           i = 0;
           j = 0;
           while(i<numNodes) {
             j = 0
                   while(j<numNodes){
                   //  print(MDarray(i)(j) + "  ");
                     j = j + 1;
                  }
              //   println("");
                  i = i + 1;
           }
        } else if(topology == "2D" || topology == "imp2D") {
                        var Skip : Array[Int] = new Array[Int](numNodes);
                        if (topology == "imp2D"){
                            flag = 1;
                            var i = 0
                            for(i<-0 to numNodes - 1){
                                    Skip(i) = 0;
                            }
                        } else {
                            flag = 0;
                        }
                        var grid:Double = math.sqrt(numNodes.toDouble)
                        while(i<numNodes){
                                if((i+1)%grid == 0){
                                        if(i+grid.toInt < numNodes){
                                                MDarray(i)(i+grid.toInt) = 1;
                                                MDarray(i+grid.toInt)(i) = 1;
                                        }
                                } else {
                                        MDarray(i)(i+1) = 1;
                                        MDarray(i+1)(i) = 1;
                                        if(i+grid.toInt < numNodes) {
                                                MDarray(i)(i+grid.toInt) = 1;
                                                MDarray(i+grid.toInt)(i) = 1;
                                        }
                                }
                                if(flag == 1) {
                                        var counter:Int = 0
                                        var stop:Int = 0
                                        if (Skip(i) == 0) {
                                                var r = new scala.util.Random
                                                val range = 0 to (numNodes-1);
                                                var tempRand = r.nextInt(range.length);
                                                while((MDarray(i)(tempRand) == 1 || Skip(tempRand) == 1 || i==tempRand) && stop == 0 ) {
                                                        tempRand = r.nextInt(numNodes);
                                                        counter +=1;
                                                        if (counter > numNodes){
                                                                stop = 1;
                                                                tempRand = i;
                                                        }
                                                }
                                        /////////////////////////////////println(tempRand + "cURRENT I :" + i)
                                        MDarray(i)(tempRand) = 1;
                                        MDarray(tempRand)(i) = 1;
                                        Skip(tempRand) = 1;
                                        Skip(i) = 1;

                                        }
                                }
                                i = i + 1;
                        }
                        i = 0;
                        j = 0;
                        while(i<numNodes) {
                                j = 0
                                while(j<numNodes){
                                  //      print(MDarray(i)(j) + "  ");
                                        j = j + 1;
                                }
                           //     println("");
                                i = i + 1;

                        }
     }  else if(topology == "line"){
                  while(i +1<numNodes){
                    MDarray(i)(i+1) = 1;
                    MDarray(i+1)(i) = 1;
                    i = i + 1;
                  }
                  i = 0;
                  j = 0;
                  while(i<numNodes) {
                          j = 0
                          while(j<numNodes){
                         //    print(MDarray(i)(j) + "  ");
                             j = j + 1;
                          }
                       //   println("");
                          i = i + 1;
                  }
        }

 // val mon = new Monitor(numNodes)
  //mon.start()
 
  var full_buff = new ArrayBuffer[Act1]()
        
  val mon = new Monitor(numNodes, full_buff,dropCount,gossipFlag)
  mon.start()
  
 
  var k = 0
  while(k<numNodes){
    var act11:Act1 = null
    if(k ==0){
       act11 = new Act1((k+1).toDouble,1.0,k,mon,numNodes,gossipFlag)
    } else {
      act11 = new Act1((k+1).toDouble,0.0,k,mon,numNodes,gossipFlag)
    }
    act11.start()
    full_buff += act11
    k += 1
  }
  
  k =0
  while(k<numNodes){
    var temp_buff = new ArrayBuffer[Act1]()
    var l = 0
    while(l<numNodes){
      if(MDarray(k)(l) == 1){
        temp_buff += full_buff(l)
      }
      l += 1
    }
    full_buff(k)!temp_buff
    k += 1
  }
  var b = System.currentTimeMillis;
  mon !("Time","Value",b)
  k = 0
 
  while(k<numNodes){
    full_buff(k)!"start"
    k = k+1
  }

}
}

class Monitor(var num:Int, var full_buff:ArrayBuffer[Act1],var dropCount:Int,var gossipFlag:Int)extends Actor{
  
    def defined[T,U](hash: HashMap[T, U], key: T) = hash.contains(key)
    val mode = new HashMap[Double,Int]
	println("total number of nodes are" + num)
	var num1 = num
	var boo :Array[Int] = new Array[Int](num)
	var i = 0
	var maxCount = 0
	var Mode:Double = 0
	var reactWithinTime = 100000;
	var ExpectedResult : Double = num.toDouble * (num.toDouble + 1) / 2
	var b:Long = 0;
	while(i<num){
		boo(i) = 0
				i = i+1
	}
	def act(){
		var ids:String = ""
		var k =0
		loop{
			reactWithin(reactWithinTime){//}react{
			    case x:(String,String,Long) =>
			      b = x._3
				case x:(Int,Double) =>
				  var inc = 1;
					/////////////////////////////////println("Received converge from " + x._1)
					/*var modecount = defined(mode, x._2)
					if (modecount){
					  mode(x._2) = 0
					} else {*/
					  var modeCount = mode.getOrElseUpdate(x._2, 1)
					  if (modeCount > 0 ){					    
					    mode.put(x._2,modeCount + 1)
					    if (modeCount + 1 > maxCount){
					      maxCount = modeCount + 1 
					      Mode = x._2
					    }
					  }					 
				
					ids = ids + (x._1).toString+" : "+(x._2).toString+" ,"
					num = num - 1
					boo(x._1) = 1
					/////////////////////////////////println("Pending nodes so far " + num + " . finished ids are :" + ids + " ******************************************")
					/////////////////////////////////print("Nodes yet to be converged are : ")
					i = 0
					while(i<num1){
						if(boo(i) == 0){
							print(i+",")		
						}
						i = i + 1
					}
					println("")
					if (num == 0){
					  println("All nodes are converged. End of the Program.")
					  if(gossipFlag == 0){
						println("Mode Value :" + Mode)
						println("Deviation from Expected Result :" + (ExpectedResult - Mode))
					  }
					 println("Time Taken :" + (System.currentTimeMillis - b))
					  exit()
					}
					
					if(num1-num == dropCount){
					  k = num1 -1
					 // println("Am I here " + k)
					   while (full_buff(k).getState == scala.actors.Actor.State.Terminated && k > 0) {
						  k -= 1						  				 
					   }
					  //println("Am I here again" + k)
					  full_buff(k) ! "End"
					//  println("dsfjhsdkjhgddfkgmndfmkgnfdkgnfdlkjgndflkgjndflkgjnkkdjfgkfd to " + k)
					}
					
					reactWithinTime = 3000
				case TIMEOUT => 
					//println("Bye Bye Birdie")
					k=0;
					while(k<num1){
					  if (full_buff(k).getState != scala.actors.Actor.State.Terminated) {						 
						  full_buff(k) ! "End"						  
					  }
					 k += 1
					}
					var perc:Double = 100 - num.toDouble/num1.toDouble * 100;
					println("% Nodes Converged : " + perc+ "%")
					if(gossipFlag == 0){
						println("Mode : " + Mode)
						println("Deviation from Expected Result :" + (ExpectedResult - Mode))
					}
					println("Time Taken :" + (System.currentTimeMillis - b))
					exit()
			}					
		}
	}
}

class Act1(var sum :Double,var wt : Double, id : Int, mon :Monitor,numNodes:Int,var gossipFlag:Int) extends Actor{
  def act(){
    var gossipCount = 0;
    var sw_ratio :Double = 0.0
    var sw_ratio_prev : Double = 0.0
    //var conv_limit : Double = 0.00000000001
    var conv_limit : Double = 0.00000000001
    var conv_count : Int = 0
    var rx_count :Int = 0
    var rx_limit :Int = 5000
    var stop:Int = 0
    var conn  =  new ArrayBuffer[Act1]()
    var rand: Int = 0
    var se = Actor.self
    var tx_count:Int = 0
    var tx_limit :Int = 5000
    var total_tx : Int = 0
    var conn_length = 4
    var rand1 = new scala.util.Random
    var i:Int = 0
    var do_nothing:Int = 1
    var lost_conn :Int = 0
    loop {
      react{
        case myarray:ArrayBuffer[Act1] =>
            conn = myarray
            conn_length = myarray.length
            /////////////////////////////////println(id + "received the connection array and conn length is "+ conn_length)
     /*   case "Gossip" =>
            gossipCount += 1
            if (gossipCount == 1000){
              
               //println(id + " current s/w is " + sw_ratio)
                          println(id +" Gossip Done" + gossipCount)
                          println(id +":reached convergence so exiting######")
                          mon ! (id,1.00000001)
                           i = 0///////////////////this is the new code
                           while(i<(25)){//(conn_length)){ // sending msgs two times the number of conn lenght times.
                             var temp = 0
                             while ((conn(rand).getState == scala.actors.Actor.State.Terminated )&& temp < 2* conn_length){
                                           rand = rand1.nextInt(conn_length)
                                           temp += 1
                                   }
                                   //println(id + " sending message to " + rand)
                                   conn(rand)! "Gossip"
                                  /* sum = sum/2
                                   wt = wt/2
                                   rand = rand1.nextInt(conn_length)*/
                                   i += 1
                           }
                          //////////////////till here
                                do_nothing = 0
                          for (ind<- 0 until conn_length-1){
                            conn(ind)!id
                          }
                          exit()
              
              
            }*/
        case "start"=>
                //println(id+ "received start")
            if(wt != 0){
                          sw_ratio_prev = sum/wt
                        }
             i = 0
            while(i<2){//(conn_length)){ // sending msgs two times the number of conn lenght times.
                while (conn(rand).getState == scala.actors.Actor.State.Terminated ){
                     rand = rand1.nextInt(conn_length)
                }
              
	            conn(rand)! (sum/2,wt/2,id)
	            sum = sum/2
	            wt = wt/2
	            rand = rand1.nextInt(conn_length)                 
                i += 1
            }
//            if(id ==0){
//              Actor.self!(1,2)
//            }
        case x:(Double,Double,Int)  =>
                if(do_nothing == 1){
              
                    rx_count = rx_count+1
                    tx_count = 0
                    if(wt != 0){
                      sw_ratio_prev = sum/wt
                    }                 
                    sum = sum + x._1                    
                    wt = wt + x._2                  
                    sw_ratio = sum/wt
                   
                    if(((sw_ratio - sw_ratio_prev) < conv_limit) && ((sw_ratio - sw_ratio_prev)> -conv_limit)) {
                      conv_count +=1                   
                    } else {
                      conv_count = 0                   
                    }
                    
                    /*********************/
                    
                    if(gossipFlag == 1){
                      if(rx_count == 10000){
                        conv_count = 3
                      }
                    }
                    /*********************/

                    if(rx_count >= rx_limit) {                    
                     stop =1
                    }
                    if(conv_count >2){
                               
                         ///////////////////////////////// println(id +" :my rx count is  "+rx_count+" and my tx_count is  "+total_tx)
                         ///////////////////////////////// println(id +":reached convergence so exiting######")
                           if(gossipFlag == 1){
                        	   mon ! (id,rx_count.toDouble)
                           } else {
                        	   mon ! (id,sw_ratio)
                           }
                           i = 0///////////////////this is the new code
                           while(i<(25)){//(conn_length)){ // sending msgs two times the number of conn lenght times.
                                 var temp = 0
                             while ((conn(rand).getState == scala.actors.Actor.State.Terminated )&& temp < 2* conn_length){
                                           rand = rand1.nextInt(conn_length)
                                           temp += 1
                                   }
                                   //println(id + " sending message to " + rand)
                                   conn(rand)! (sum/2,wt/2,id)
                                   sum = sum/2
                                   wt = wt/2
                                   rand = rand1.nextInt(conn_length)
                                   i += 1
                           }
                          //////////////////till here
                                do_nothing = 0
                          for (ind<- 0 until conn_length-1){
                            conn(ind)!id
                          }
                          exit()
                    }
                    if(lost_conn== conn_length){
                   /////////////////////////////////  println("All my neighbors have exited!!!!!!!!!")
                    }
                    if(stop == 0 && tx_count < tx_limit && do_nothing == 1 && (lost_conn!= conn_length)){
                    //if(stop == 0){
                            i = 0
                      while(i<1){
                        i += 1

                            tx_count += 1
                            total_tx += 1
                            var temp = 0
                            //println(id+"random value is "+rand)
                            while ((conn(rand).getState == scala.actors.Actor.State.Terminated) && temp != 2*conn_length ){
                                    rand = rand1.nextInt(conn_length)
                                    temp += 1
                            }
                       // println(id + ": sending a message to "+conn(rand) + " "+rand)
                            //println("random value is "+ rand + " " + conn(rand).getState)
                            conn(rand)! (sum/2,wt/2,id)
                            sum = sum/2
                            wt = wt/2
                            rand = rand1.nextInt(conn_length)
                            //  rand = (rand +1)%(conn_length)
                        }
                    }	
                }
                //Actor.self! (1,2)
        case x : Int => lost_conn += 1
                        //println(id+" received exit from my neighbor" +x +" and my lost connections are " + lost_conn)
                var temp = 0
                while ((conn(rand).getState == scala.actors.Actor.State.Terminated) && temp != 2*conn_length ){
                                        rand = rand1.nextInt(conn_length)
                                        temp += 1
                                }
                           // println(id + ": sending a message to "+conn(rand) + " "+rand)
                                //println("random value is "+ rand + " " + conn(rand).getState)
                                conn(rand)! (sum/2,wt/2,id)
                                sum = sum/2
                                wt = wt/2
                                rand = rand1.nextInt(conn_length)

        case x:(Int,Int) =>
                if(stop == 0 && tx_count < tx_limit){
         //if(stop == 0){
                      /////////////////////////////////  println(id + ": sending a message to "+conn(rand) + " "+rand)
                  tx_count += 1
                  total_tx += 1
                   while (conn(rand).getState == scala.actors.Actor.State.Terminated ){
                   ///////////////////////////////// println("current state is " + conn(rand).getState)
                   ///////////////////////////////// println(scala.actors.Actor.State.Terminated)
                    rand = rand1.nextInt(conn_length)
                  }

                  conn(rand)! (sum/2,wt/2,id)
                  sum = sum/2
                  wt = wt/2

                  rand = rand1.nextInt(conn_length)
                  //rand = (rand +1)%(conn_length)
                  }
//              }else if (stop == 1) {
//                println(id +":reached limit so exiting######")
//                mon ! id
//                exit()
//              }
                Actor.self ! (1,2)
        case "End" =>
              //  println("Blah Balh End")
        	exit()
      }
    }
  }
}