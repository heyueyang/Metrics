package metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * �ʴ��࣬���ڽ�һЩԴ����ϢתΪ����������ʽ��Ŀǰת���������ֵ������û���ǡ�
 * ��Դ�밴����ע�������Ƿ�ע����һ���Ľ�������֡������Ƕ���ע���������ݣ�ע���еĴ���*��
 * ���ܵ��������ġ�
 * @author niu
 *
 */
public class Bow {
	Map<String, Integer> bag;
	String[] dictory2 = { "!=", "==", "++", "--", "||", "&&", "<=", ">=" };
	String[] dictory1 = { "=", "+", "-", "*", "/", "%", "!", "?" };
	String[] dictory3 = { "=", "!=", "+", "*", "-", "||", "/", "&", "%", "!",
			"?", ">=", "<=", "<", ">" }; // ȥ��ע���л����ַ����е�������š�

public static void main(String[] args) throws Exception {

		String test_path = "/home/yueyang/data/error/5329_3233_12_error.txt";
		
		BufferedReader bReader = new BufferedReader(new FileReader(test_path));
		StringBuffer sBuffer = new StringBuffer();
		String line = null;
		while ((line = bReader.readLine()) != null) {
			sBuffer.append(line);
		}
		Bow b = new Bow();
		b.bowP(sBuffer);
		
	}

	public Map<String, Integer> bow(String text) {
		bag = new HashMap<String, Integer>();
		int startIndex = 0;
		int endIndex = 0;
		while (endIndex <= text.length() - 1) {
			while (endIndex <= text.length() - 1
					&& (!isCharacter(text.charAt(endIndex)))) {
				endIndex++;
			}
			startIndex = endIndex;
			while ((endIndex <= text.length() - 1)
					&& isCharacter(text.charAt(endIndex))) {
				endIndex++;
			}
			String subString = text.substring(startIndex, endIndex);
			subString = subString.toLowerCase();
			if (bag.keySet().contains(subString)) {
				bag.put(subString, bag.get(subString) + 1);
			} else {
				bag.put(subString, 1);
			}
			while (endIndex <= text.length() - 1
					&& (!isCharacter(text.charAt(endIndex)))) {
				endIndex++;
			}
			startIndex = endIndex;
		}
		return bag;
	}

	public void printBag() {
		System.out.println(bag);
	}

	public boolean isCharacter(char c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return true;
		}
		return false;
	}

	/**
	 * ��ȡ�ӵ�ǰλ�ÿ�ʼtext��һ�γ���ע�ͻ����ַ����ĵط���
	 * 
	 * @param text
	 *            Դ������
	 * @param start
	 *            ��ǰ����λ��
	 * @return ��һ�γ���ע�ͻ����ַ����ĵط�
	 */
	public int getIndex(StringBuffer text, int start) {
		while (start < text.length()) {
			// /* is the begin of comment
			if (start < text.length() - 1
					&& text.substring(start, start + 2).equals("/*")) {
				break;
			}
			//'"' is not real the begin og "", so we ignore it 
			if (start < text.length() - 1 && start > 1
					&& text.charAt(start) == '"'
					&& text.charAt(start - 1) == '\''
					&& text.charAt(start + 1) == '\'') {
				start++;
				continue;
			}
			// \", \\\", " is the begin of "",
			if ((start > 1 && text.charAt(start) == '"' && text
					.charAt(start - 1) != '\\')
					|| (start > 2 && text.substring(start - 2, start + 1)
							.equals("\\\\\""))
					|| (start == 1 && text.charAt(start) == '"')) {
				break;
			}
			// // is the begin of comment
			if (start < text.length() - 1
					&& text.substring(start, start + 2).equals("//")) {
				break;
			}
			start++;
		}
		return start;
	}

	/**
	 * �����ӵĴ���ע�͵�Դ���ļ�תΪ�ʴ���ʽ��
	 * 
	 * @param text
	 *            Դ������
	 * @return ת����Ĵʴ�
	 */
	public Map<String, Integer> bowP(StringBuffer text) {
		StringBuffer hunkBuffer = new StringBuffer();
		bag = new HashMap<String, Integer>();
		int i = 1;
		while (text.toString().length() > 0) {// ��Դ��������ֱ�Ӽ���hunkBuffer����ע�������ݴ�������hunkBuffer��
			int start = 0;
			start = getIndex(text, start);
			if (start==text.length()) {   //���startֱ���ҵ����ļ�ĩβ����˵������û��ע������
				hunkBuffer.append(" " + text.substring(0, start));
				break;
			}
			while (text.charAt(start) == '"') { // ����ҵ��ĵ�һ��ע�����ַ�Ϊ���������ǰ����\��ô�Ͳ���������ע�����Ŀ�ʼ��
				if (start > 0 && text.charAt(start - 1) == '\\') { // ����һ���ַ��������Ŀ�ʼ���߽�����
					start++;
					start = getIndex(text, start);
				} else {
					break;
				}
			}
			// ִ�е��˴�Ҫô�����ĵ�ĩβ��Ҫô��Ȼ�ҵ���ע�����Ŀ�ʼ��
			if (start == text.length() - 1) { // �����ĵ������������һ���ַ�����ô��ֱ���˳���
				hunkBuffer.append(text);
				break;
			}
			// ��һ��ע����ǰ��Ķ���Դ�����ݣ�����hunkBuffer�������ƺ�����Ҫ������ո�������ˣ�����ķ���Ӧ��һ�������ո�֡�
			hunkBuffer.append(" " + text.substring(0, start));
			text.delete(0, start); // ��text�д����������ɾ��
			
			start = 0; // ��ָ��ָ��textͷ����
			String startOper = new String();
			if (text.charAt(start) == '/') { // ƥ��֮ǰ���ֵĲ�������ȷ��ע������
				if (text.charAt(start + 1) == '*') {
					startOper = "/*";
				} else {
					startOper = "//";
				}
			} else {
				startOper = "\"";
			}
			String rage;

			if (startOper.equals("//")) { // ȷ����ǰע������Χ�������˫б�ܣ���removeSC2��������ע�������ݡ�
				int inedex = text.indexOf("\n");
				if (inedex == -1) { // ���һ����ע��
					rage = text.substring(start + 2, text.length());
					text = null;
				} else { // ֮��������
					rage = text.substring(start + 2, text.indexOf("\n"));
					text.delete(0, text.indexOf("\n") + 1);
				}
				hunkBuffer.append(" " + removeSC2(rage));
			} else if (startOper.equals("/*")) {
				rage = text.substring(start + 2, text.substring(2).indexOf("*/")+2);
				hunkBuffer.append(" " + removeSC2(rage));
				text.delete(0, text.substring(2).indexOf("*/") + 4);
			} else {
				text.deleteCharAt(0);
				int tail = text.indexOf("\"");
				while (tail >=1) {
					int numl = 0;
					for (int j = tail - 1; j >= 0; j--) {
						if (text.charAt(j) == '\\') {
							numl++;
						} else {
							break;
						}
					}
					if (numl % 2 == 0) {
						break;
					} else {
						tail = tail + 1;
						tail = tail
								+ text.substring(tail, text.length()).indexOf(
										'"');
					}
				}
				rage = text.substring(0, tail);
				i++;
				hunkBuffer.append(" " + removeSC2(rage));
				text.delete(0, tail + 1);
			}
		}

		String dirList[] = hunkBuffer.toString().split(
				"[\\.\\s\\)\\(;:,\"\\[\\]\\{\\}]|//]"); 
		for (String string : dirList) {
			if (!string.equals("")) { // ��仰�ǲ���Ҳ�����Ż���
				boolean contain = false;
				for (String oper : dictory2) {
					contain = diviOper(oper, string, bag);
					if (contain == true) {
						break;
					}
				}
				if (contain == false) {
					for (String oper2 : dictory1) {
						contain = diviOper(oper2, string, bag);
						if (contain == true) {
							break;
						}
					}
				}
				if (contain == false) {
					// <>���ǲ����������
					if (string.contains("<") || string.contains(">")) {
						String[] divTempStrings = string.split(">|<");
						for (String string2 : divTempStrings) {
							if (!string2.equals("")) {
								putInBag(string2, bag);
							}
						}
					} else {
						putInBag(string, bag);
					}
				}
			}
		}
		return bag;
	}

	private String removeSC2(String rage) {
		for (String string : dictory3) {
			rage = rage.replace(string, " ");
		}
		return rage;
	}

	/**
	 * �ж��ַ������Ƿ�����Ų�����������������߱����Լ����ǲ������򽫶��߷ֿ���Ȼ��ֱ����bag��
	 * ��Ҫע��������һ���ַ����а�����<������>������Ϊ���ǲ��������Ա��java��ʹ�úܶ��<���֣�
	 * ����˵��Ĭ������Ǽ��ŵĻ�һ��û�кͱ���������һ��
	 * 
	 * @param oper
	 *            ���ԵĲ�����
	 * @param string
	 *            ���Ե��ַ���
	 * @param bag
	 *            Ҫ�����bag
	 * @return �Ƿ�������������߱����ǲ�����
	 */
	public boolean diviOper(String oper, String string, Map<String, Integer> bag) {
		if (string.equals(oper)) {
			putInBag(string, bag);
			return true;
		}
		String diOperString = oper;
		if (string.contains(oper)) {
			if (oper.equals("++")) {
				diOperString = "\\+\\+";
			} else if (oper.equals("+")) {
				diOperString = "\\+";
			} else if (oper.equals("?")) {
				diOperString = "\\?";
			} else if (oper.equals("*")) {
				diOperString = "\\*";
			}
			String[] divide1 = string.split(diOperString);
			for (String string2 : divide1) {
				if (!string2.equals("")) {
					putInBag(string2, bag);
				}
			}
			putInBag(oper, bag);
			return true;
		}
		return false;
	}

	public void putInBag(String string, Map<String, Integer> map) {
		if (map.containsKey(string)) {
			map.put(string, map.get(string) + 1);
		} else {
			map.put(string, 1);
		}
	}

	public Map<String, Integer> bowPP(String text) {
		bag = new HashMap<>();
		String dirList[] = text.split("/");
		String regex = ".*[A-Z].*";
		for (String string : dirList) {
			if (string.matches(regex)) {
				int startIndex = 0;
				int endIndex = 1;
				while (endIndex < string.length()) {
					while (endIndex < string.length()
							&& (!Character.isUpperCase(string.charAt(endIndex)))) {
						endIndex++;
					}
					String temp = string.substring(startIndex, endIndex)
							.toLowerCase(); // ֮ǰû�д����Сдת�����⡣
					if (bag.keySet().contains(temp)) {
						bag.put(temp, bag.get(temp) + 1);
					} else {
						bag.put(temp, 1);
					}
					startIndex = endIndex;
					endIndex = endIndex + 1;
					if (endIndex >= string.length()
							&& startIndex < string.length()) {
						if (bag.keySet().contains(string.charAt(startIndex))) {
							bag.put(string.charAt(startIndex) + "",
									bag.get(string.charAt(startIndex) + "") + 1);
						} else {
							bag.put(string.charAt(startIndex) + "", 1);
						}
					}
				}
			} else {
				if (bag.keySet().contains(string)) {
					bag.put(string, bag.get(string) + 1);
				} else {
					bag.put(string, 1);
				}
			}
		}
		return bag;
	}
}