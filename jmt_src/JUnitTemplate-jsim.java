	@Test
	public void testName() throws Exception {
		
	File input = File.createTempFile("testName", ".xml");
		File result = File.createTempFile("testName-result", ".xml");
		File expected = new File(getClass().getResource("testName-output.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("testName-input.xml").toURI()), input);

		String[] args = {"","","-maxtime","60","-seed","1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options)); 
		
		/* This part of the code removes the first row from the result file */
		Scanner scanner = new Scanner(result);
		ArrayList<String> coll = new ArrayList<String>();
		scanner.nextLine();
		scanner.nextLine();
		while (scanner.hasNextLine()) {
		    String line = scanner.nextLine();
		    coll.add(line);
		}

		scanner.close();

		FileWriter writer = new FileWriter(result);
		for (String line : coll) {
		    writer.write(line);
		}

		writer.close();
		/* The row has now been removed */
		
		String strExp = FileUtils.readFileToString(expected, "utf-8");
		String strRes = FileUtils.readFileToString(result, "utf-8");
		String strDiff = StringUtils.difference(strExp,strRes);
			
		assertTrue(": Real output differs from expected!\n"	+ "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length()==0);
			
	}
