public class birthdayPresentsTester {
    public static void main(String[] args){
        birthdayPresents bp = new birthdayPresents();
        if(args.length == 1 && args[0].toLowerCase().equals("true")){
           bp.printOutput = Boolean.parseBoolean(args[0]);
        }
        long totalTime = 0;
        for(int i = 0; i < 50; i++){
            totalTime += bp.run(bp.printOutput);
        }
        System.out.println("Average time taken: " + (totalTime / 50) + " ms");
    }
}
