#!/usr/bin/perl -w
 
# (1) quit unless we have the correct number of command-line args
$num_args = $#ARGV + 1;
if ($num_args < 2) {
    print "\nPass as an argument a path to a file, a name for the test, and the path to the JMT jar\n";
    exit;
}
 
# (2) we got two command line args, so assume they are the
# filename and test name
$filename=$ARGV[0];
$testname=$ARGV[1];
$jmt=$ARGV[2];

system("java -cp $jmt jmt.commandline.Jmt sim ".$filename." -seed 1 -maxtime 60");

$expectedOutputName = $filename."-result.jsim";
$junitFile="JUnitTemplate-jsim.java";

system("perl -pi -e \'\$_ = \"\" if ( \$. == 1 );\' $expectedOutputName");
system("perl -pi -e \'\$_ = \"\" if ( \$. == 1 );\' $expectedOutputName");
system("perl -pi -e 'chomp unless(/^:/);' $expectedOutputName");
system("mv $expectedOutputName $testname-output.xml");
 
open(my $fh, '<:encoding(UTF-8)', $filename) or die "Could not open file '$filename' $!";
my $fullstring;
while (my $row = <$fh>) {
  $fullstring = $fullstring . $row;
  #print "ROW: $row\n";
}
my $inputText = substr($fullstring, 0, index($fullstring, "<results")) . substr($fullstring, rindex($fullstring, "/results>") + length("/results>"));

$inputName = $testname . "-input.xml";
open(my $inputFile, '>', $inputName) or die "Could not open file '$inputName' $!";
print $inputFile $inputText;
close $inputFile;

open(my $junitTemplate, '<:encoding(UTF-8)', $junitFile) or die "Could not open file '$junitFile' $!";
my $fullTemplate;
while (my $row = <$junitTemplate>) {
  chomp $row;
  $fullTemplate = $fullTemplate . "$row\n";
}
$fullTemplate =~ s/testName/$testname/g;
$junitName = $testname . "Test.java";
open(my $inputJunit, '>', $junitName) or die "Could not open file '$junitName' $!";
print $inputJunit $fullTemplate;
close $inputJunit;


print "-----Created resource files: '$inputName' '$expectedOutputName' and a JUnit test method in file: '$junitName'\n";
print "-----Paste the JUnit test method to one of the Java classes in an appropriate test package.\n";
print "-----Move the resource files to the resource folder, corresponding to the class the JUnit test method is in\n";
