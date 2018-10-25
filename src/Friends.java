/*	Pengrui liu 
  	157003935
	Jiaxu su
	160005534
	*/

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Friends {
	private static Map<String, String> menu;
	private static Graph graph;
	private static Map<String, FriendshipSP> friendshipSPs = new HashMap<>(0);
	private static Set<Student> connectors;

	static {
		menu = new HashMap<>(0);
		menu.put("1", "students at a school");
		menu.put("2", "shortest intro chain");
		menu.put("3", "cliques at school");
		menu.put("4", "connectors");
		menu.put("5", "quit");
	}

	private static class Graph {
		private final int numOfStudent;
		private int numOfFriendship;
		private Student[] students;
		private static Map<String, Integer> studentIndexTable;

		// Create graph based on graph file
		public Graph(String graphFilePath) throws IOException {
			try (Scanner file = new Scanner(new FileInputStream(graphFilePath))) {
				// Read the number of people
				numOfStudent = Integer.parseInt(file.nextLine());
				int count = 0;
				// Construct friendship graph
				// Read students' info
				students = new Student[numOfStudent];
				studentIndexTable = new HashMap<>(0);
				numOfFriendship = 0;
				while (count < numOfStudent && file.hasNextLine()) {
					String line = file.nextLine();
					String[] tokens = line.split("\\|");
					String studentName = tokens[0];
					boolean isStudent = tokens[1].equals("y");
					String school = null;
					if (isStudent)
						school = tokens[2];
					Student student = new Student();
					student.setName(studentName);
					student.setIsStudent(isStudent);
					student.setSchool(school);
					students[count] = student;
					studentIndexTable.put(student.getName(), count);
					count++;
				}

				// add friends for each student
				while (file.hasNextLine()) {
					String line = file.nextLine();
					String[] tokens = line.split("\\|");
					String fromName = tokens[0];
					String toName = tokens[1];
					int fromIndex = lookupStudentIndex(fromName);
					Student fromStudent = students[fromIndex];
					int toIndex = lookupStudentIndex(toName);
					Student toStudent = students[toIndex];
					fromStudent.addFriend(students[toIndex]);
					toStudent.addFriend(students[fromIndex]);
					numOfFriendship++;
				}
			}
		}

		public int getNumOfStudent() {
			return numOfStudent;
		}

		public int getNumOfFriendship() {
			return numOfFriendship;
		}

		public Student[] getStudents() {
			return students;
		}

		public boolean doseStudentExist(String name) {
			return studentIndexTable.containsKey(name);
		}

		public int lookupStudentIndex(String name) {
			return doseStudentExist(name) ? studentIndexTable.get(name) : -1;
		}

		public Student getStudentByName(String name) {
			return doseStudentExist(name) ? students[studentIndexTable
					.get(name)] : null;
		}

		public Student getStudentByIndex(int index) {
			return (index >= 0 || index < students.length) ? students[index]
					: null;
		}
	}

	private static class Student {
		private String name;
		private String school;
		private List<Student> friends;
		private boolean isStudent;

		public Student() {
			friends = new LinkedList<>();
		}

		public List<Student> getFriends() {
			return friends;
		}

		public void setFriends(List<Student> friends) {
			this.friends = friends;
		}

		public void addFriend(Student student) {
			this.friends.add(student);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSchool() {
			return school;
		}

		public void setSchool(String school) {
			this.school = school;
		}

		public boolean isStudent() {
			return isStudent;
		}

		public void setIsStudent(boolean isStudent) {
			this.isStudent = isStudent;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public String toString() {
			return String
					.format("(%s%s)", name, isStudent ? ", " + school : "");
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Student
					&& name.equals(((Student) obj).getName())
					&& school.equals(((Student) obj).getSchool());
		}
	}

	private static class FriendshipSP {
		private Map<Student, Integer> distTo;
		private Map<Student, Student> friendshipTo;
		private LinkedList<Student> queue;
		private Set<Student> visitedStudent;

		public FriendshipSP(Graph graph, String fromName) {
			Student from = graph.getStudentByName(fromName);
			distTo = new HashMap<>(0);
			friendshipTo = new HashMap<>(0);

			for (Student s : graph.getStudents()) {
				distTo.put(s, Integer.MAX_VALUE);
			}
			distTo.put(from, 0);

			// Bellman-Ford algorithm
			queue = new LinkedList<>();
			visitedStudent = new HashSet<>(0);
			queue.addLast(from);
			visitedStudent.add(from);
			while (!queue.isEmpty()) {
				Student s = queue.removeFirst();
				relax(s);
			}
		}

		private void relax(Student from) {
			for(Student to :from.getFriends())
			{
				if (distTo.get(to) > distTo.get(from) + 1) {
					distTo.put(to, distTo.get(from) + 1);
					friendshipTo.put(to, from);
					if (!visitedStudent.contains(to)) {
						queue.add(to);
						visitedStudent.add(to);
					}
				}
				
			}
		}

		public Map<Student, Integer> getDistTo() {
			return distTo;
		}

		public void setDistTo(Map<Student, Integer> distTo) {
			this.distTo = distTo;
		}

		public Map<Student, Student> getFriendshipTo() {
			return friendshipTo;
		}

		public void setFriendshipTo(Map<Student, Student> friendshipTo) {
			this.friendshipTo = friendshipTo;
		}

		public boolean hasFriendship(Student to) {
			return distTo.get(to) != Integer.MAX_VALUE;
		}

		public Stack<Student> friendshipChain(Student to) {
			Stack<Student> friendshipChain = new Stack<>();
			if (!hasFriendship(to))
				return null;
			for (Student s = to; friendshipTo.get(s) != null; s = friendshipTo
					.get(s)) {
				friendshipChain.push(s);
			}
			return friendshipChain;
		}
	}

	private static void printMenu() {
		System.out.println("Please select an option: ");
		for(String k: menu.keySet())
		{
			String v=menu.get(k);
			System.out.format("%s. %s%n", k, v);
		}
		System.out.println();
	}

	private static void select(String choice) {
		switch (choice) {
		case "1":
			printStudentList();
			break;
		case "2":
			introFriend();
			break;
		case "3":
			printCliques();
			break;
		case "4":
			printConnectors();
			break;
		default:
			System.exit(0);
		}
	}

	private static void printStudentList() {
		Scanner in = new Scanner(System.in);
		System.out.print("Name of school: ");
		String school = in.nextLine();
		System.out.println();
		printStudentList(school);
	}

	private static void printStudentList(String school) {
		StringBuilder builder = new StringBuilder();
		for (Student student : graph.getStudents()) {
			if (student.isStudent && student.getSchool().equals(school)) {
				builder.append(student);
				builder.append(", ");
			}
		}

		if (builder.length() == 0) {
			System.out.println("There is not student in selected school");
		} else {
			builder.delete(builder.length() - 2, builder.length());
			System.out.println(builder);
		}
		System.out.println();
	}

	private static void introFriend() {
		Scanner in = new Scanner(System.in);
		System.out.print("Name of the person wants the intro: ");
		String fromName = in.nextLine();
		System.out.print("Other person: ");
		String toName = in.nextLine();
		System.out.println();
		System.out.println(introFriend(fromName, toName));
		System.out.println();
	}

	// Find the shortest path from student "nameFrom" to student "nameTo"
	private static String introFriend(String nameFrom, String nameTo) {
		if (graph.lookupStudentIndex(nameFrom) == -1) {
			return String
					.format("Graph doesn't contain the student with name: %s",
							nameFrom);
		}

		if (graph.lookupStudentIndex(nameTo) == -1) {
			return String.format(
					"Graph doesn't contain the student with name: %s", nameTo);
		}

		Student to = graph.getStudentByName(nameTo);
		FriendshipSP sp;
		// If already computed SP
		if (friendshipSPs.containsKey(nameFrom)) {
			sp = friendshipSPs.get(nameFrom);
		} else {
			sp = new FriendshipSP(graph, nameFrom);
			friendshipSPs.put(nameFrom, sp);
		}

		if (sp.hasFriendship(to)) {
			Stack<Student> friendshipChain = sp.friendshipChain(to);

			StringBuilder builder = new StringBuilder();
			builder.append(graph.getStudentByName(nameFrom));
			builder.append("--");
			while (!friendshipChain.empty()) {
				builder.append(friendshipChain.pop());
				builder.append("--");
			}
			builder.delete(builder.length() - 2, builder.length());
			return builder.toString();
		}
		return String.format("No relationship between %s and %s", nameFrom,
				nameTo);
	}

	private static void printCliques() {
		Scanner in = new Scanner(System.in);
		System.out.print("Name of school: ");
		String school = in.nextLine();
		System.out.println();
		printCliques(school);
	}

	// Print out cliques
	private static void printCliques(String school) {
		LinkedList<Student> queue = new LinkedList<>();
		Set<Student> visitedStudent = new HashSet<>(0);
		Set<Student> printedStudent = new HashSet<>(0);
		Student student;
		int cliqueCount = 0;

		for (int i = 0; i < graph.getNumOfStudent(); i++) {
			student = graph.getStudentByIndex(i);
			if (student.isStudent() && student.getSchool().equals(school)
					&& !printedStudent.contains(student)) {
				queue.clear();
				visitedStudent.clear();
				queue.add(student);

				// BFS
				while (!queue.isEmpty()) {
					Student currentStudent = queue.removeFirst();
					if (!visitedStudent.contains(currentStudent)) {
						for(Student e:currentStudent.getFriends())
						{
							if (e.isStudent() && e.getSchool().equals(school))
								queue.add(e);
						}
						visitedStudent.add(currentStudent);
					}
				}

				// print clique
				if (visitedStudent.size() != 0) {
					System.out.printf("Clique %d:%n", ++cliqueCount);
					System.out.println();
					System.out.println(visitedStudent.size());
					for(Student e:visitedStudent)
					{
						System.out.printf("%s|%s%n", e
								.getName(), e.isStudent ? "y|" + e.getSchool()
								: "n");	
					}
					for(Student e:visitedStudent)
					{
						for(Student f:e.getFriends())
						{
							if (!printedStudent.contains(f)
									&& f.isStudent
									&& f.getSchool().equals(school))
								System.out.printf("%s|%s%n",
										e.getName(), f.getName());
							
						}

						printedStudent.add(e);
					}
					System.out.println();
				}
			}
		}

		if (cliqueCount == 0)
			System.out.println("There is not clique in selected school");
		System.out.println();
	}

	// Print out all connectors
	private static int dfsStartingPointCount = 0;

	private static void printConnectors() {
		connectors = new HashSet<>(0);

		int[] dfsnum = new int[graph.getNumOfStudent()];
		int[] back = new int[graph.getNumOfStudent()];
		boolean[] isVisited = new boolean[graph.getNumOfStudent()];
		int countDfsnum = 0;
		for (int i = 0; i < graph.getNumOfStudent(); i++) {
			DFS(dfsnum, back, isVisited, countDfsnum, i);
		}
		if (dfsStartingPointCount > 1) {
			connectors.add(graph.getStudentByIndex(0));
		}

		System.out.println("Connectors: ");
		StringBuilder builder = new StringBuilder();
		for(Student e:connectors){
			builder.append(e);
			builder.append(", ");
		}
	
		builder.delete(builder.length() - 2, builder.length());
		System.out.println(builder);
		System.out.println();
	}

	private static void DFS(int[] dfsnum, int[] back, boolean isVisited[],
			int countDfsnum, int studentIndex) {
		if (isVisited[studentIndex])
			return;

		dfsnum[studentIndex] = countDfsnum;
		back[studentIndex] = countDfsnum;
		countDfsnum++;
		Student student = graph.getStudentByIndex(studentIndex);
		isVisited[studentIndex] = true;
		for (Student s : student.getFriends()) {
			int nextStudentIndex = graph.lookupStudentIndex(s.getName());

			if (isVisited[nextStudentIndex]) {
				back[studentIndex] = Math.min(back[studentIndex],
						dfsnum[nextStudentIndex]);
			} else {
				DFS(dfsnum, back, isVisited, countDfsnum, nextStudentIndex);
				if (dfsnum[studentIndex] > back[nextStudentIndex]) {
					back[studentIndex] = Math.min(back[studentIndex],
							back[nextStudentIndex]);
				} else {
					if (studentIndex == 0) {
						dfsStartingPointCount++;
					} else {
						connectors.add(graph.getStudentByIndex(studentIndex));
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		System.out.print("Please input the path of your graph file: ");
		Scanner in = new Scanner(System.in);
		String graphFilePath = in.nextLine();
		System.out.println();

		// Parse graph file
		try {
			graph = new Graph(graphFilePath);

			while (true) {
				printMenu();
				String choice = in.nextLine();
				select(choice);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
