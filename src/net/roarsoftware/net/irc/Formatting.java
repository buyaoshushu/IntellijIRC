package net.roarsoftware.net.irc;

import java.awt.Color;
import java.util.Map;
import java.util.HashMap;
import javax.swing.text.StyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;

/**
 *
 */
public class Formatting {

	public static final char NORMAL = '\u000f';

	public static final char BOLD = '\u0002';

	public static final char UNDERLINE = '\u001f';

	public static final char REVERSE = '\u0016';

	public static final char ITALIC = '\u001d';

	public static final Map<Integer, Color> colors = new HashMap<Integer, Color>();
	static {
		Formatting.colors.put(1, Color.BLACK); // Black
		Formatting.colors.put(2, new Color(0, 0, 128)); // Navy Blue
		Formatting.colors.put(3, new Color(0, 139, 0)); // Green
		Formatting.colors.put(4, Color.RED); // Red
		Formatting.colors.put(5, new Color(139, 69, 19)); // Brown
		Formatting.colors.put(6, new Color(153, 50, 204)); // Purple
		Formatting.colors.put(7, new Color(110, 139, 61)); // Olive
		Formatting.colors.put(8, Color.YELLOW); // Yellow
		Formatting.colors.put(9, Color.GREEN); // Lime Green
		Formatting.colors.put(10, new Color(153, 255, 204)); // Teal
		Formatting.colors.put(11, Color.CYAN); // Aqua Light
		Formatting.colors.put(12, Color.BLUE); // Royal Blue
		Formatting.colors.put(13, Color.MAGENTA); // Hot Pink
		Formatting.colors.put(14, Color.DARK_GRAY); // Dark Gray
		Formatting.colors.put(15, Color.LIGHT_GRAY); // Light Gray
		Formatting.colors.put(0, Color.WHITE); // White
	}

	private Formatting() {
	}

	public static boolean isColor(char c) {
		return (c == '\u0003');
	}

	public static Color getColor(int i) {
		return Formatting.colors.get(i);
	}

	public static boolean isBold(char c) {
		return c == Formatting.BOLD;
	}

	public static boolean isUnderline(char c) {
		return c == Formatting.UNDERLINE;
	}

	public static boolean isReverse(char c) {
		return c == Formatting.REVERSE;
	}

	public static boolean isItalic(char c) {
		return c == Formatting.ITALIC;
	}

	public static boolean isNormal(char c) {
		return c == Formatting.NORMAL;
	}

	public static boolean isFormatting(char c) {
		return (Formatting.isColor(c) || Formatting.isBold(c) || Formatting.isUnderline(c) || Formatting.isReverse(c) || Formatting
				.isNormal(c));
	}

	public static void formatText(StyledDocument doc, String text, MutableAttributeSet as) {
		try {
			if(as == null)
				as = new SimpleAttributeSet();
			else
				as = new SimpleAttributeSet(as);
			Color currentForeground = null;
			Color currentBackground = null;
			boolean bold = false, underline = false, reverse = false, italic = false;
			boolean colorJustSet = false;
			for(int i = 0, n = text.length(); i < n; i++) {
				char c = text.charAt(i);
				boolean formatChar = Formatting.isFormatting(c);
				if(formatChar) {
					if(Formatting.isColor(c)) {
						if(i + 1 < text.length()) {
							char next = text.charAt(++i);
							char afternext = (char) -1;
							if(i + 1 < text.length())
								afternext = text.charAt(++i);
							int color = next - 48;
							if(Character.isDigit(afternext)) {
								color = color * 10 + (afternext - 48);
							} else {
								i--;
							}
							int j = i;
							if(j < n - 1) {
								next = text.charAt(++j);
								if(next == ',') {
									next = text.charAt(++j);
									if(Character.isDigit(next)) {
										int bgcolor = next - 48;
										i = j;
										if(j < n - 1) {
											next = text.charAt(++j);
											if(Character.isDigit(next)) {
												bgcolor = bgcolor * 10 + (next - 48);
												i = j;
											}
										}
										Color newBg = Formatting.getColor(bgcolor);
										if(currentBackground == null)
											currentBackground = newBg;
										else if(currentBackground.equals(newBg))
											currentBackground = null;
										else
											currentBackground = newBg;
										StyleConstants.setBackground(as, (currentBackground != null ? currentBackground
												: Color.WHITE));
									}
								}
							}
							colorJustSet = true;
							Color newCol = Formatting.getColor(color);
							if(currentForeground == null)
								currentForeground = newCol;
							else if(currentForeground.equals(newCol) && !colorJustSet)
								currentForeground = null;
							else
								currentForeground = newCol;
							StyleConstants.setForeground(as, (currentForeground != null ? currentForeground
									: Color.BLACK));
						}
					} else {
						colorJustSet = false;
						if(Formatting.isBold(c)) {
							bold = !bold;
						} else if(Formatting.isUnderline(c)) {
							underline = !underline;
						} else if(Formatting.isItalic(c)) {
							italic = !italic;
						} else if(Formatting.isReverse(c)) {
							reverse = !reverse;
							StyleConstants.setBackground(as, (reverse ? Color.BLACK : Color.WHITE));
						} else if(Formatting.isNormal(c)) {
							currentForeground = null;
							bold = false;
							underline = false;
							reverse = false;
							underline = false;
						}
						StyleConstants.setBold(as, bold);
						StyleConstants.setUnderline(as, underline);
						StyleConstants.setItalic(as, italic);
					}
				} else {
					doc.insertString(doc.getLength(), String.valueOf(c), as);
				}
			}
			// doc.insertString(doc.getLength(), "ENDE", new SimpleAttributeSet());
		} catch(BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes all colours from a line of IRC text.
	 *
	 * @since PircBot 1.2.0
	 * @param line the input text.
	 * @return the same text, but with all colours removed.
	 */
	public static String removeColors(String line) {
		int length = line.length();
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		while(i < length) {
			char ch = line.charAt(i);
			if(ch == '\u0003') {
				i++;
				// Skip "x" or "xy" (foreground color).
				if(i < length) {
					ch = line.charAt(i);
					if(Character.isDigit(ch)) {
						i++;
						if(i < length) {
							ch = line.charAt(i);
							if(Character.isDigit(ch)) {
								i++;
							}
						}
						// Now skip ",x" or ",xy" (background color).
						if(i < length) {
							ch = line.charAt(i);
							if(ch == ',') {
								i++;
								if(i < length) {
									ch = line.charAt(i);
									if(Character.isDigit(ch)) {
										i++;
										if(i < length) {
											ch = line.charAt(i);
											if(Character.isDigit(ch)) {
												i++;
											}
										}
									} else {
										// Keep the comma.
										i--;
									}
								} else {
									// Keep the comma.
									i--;
								}
							}
						}
					}
				}
			} else if(ch == '\u000f') {
				i++;
			} else {
				buffer.append(ch);
				i++;
			}
		}
		return buffer.toString();
	}

	/**
	 * Remove formatting from a line of IRC text.
	 *
	 * @since PircBot 1.2.0
	 * @param line the input text.
	 * @return the same text, but without any bold, underlining, reverse, etc.
	 */
	public static String removeFormatting(String line) {
		int length = line.length();
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < length; i++) {
			char ch = line.charAt(i);
			if(ch == '\u000f' || ch == '\u0002' || ch == '\u001f' || ch == '\u0016') {
				// Don't add this character.
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	/**
	 * Removes all formatting and colours from a line of IRC text.
	 *
	 * @since PircBot 1.2.0
	 * @param line the input text.
	 * @return the same text, but without formatting and colour characters.
	 */
	public static String removeFormattingAndColors(String line) {
		return Formatting.removeFormatting(Formatting.removeColors(line));
	}
}
