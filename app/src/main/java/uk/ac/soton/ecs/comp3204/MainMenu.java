package uk.ac.soton.ecs.comp3204;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openimaj.util.processes.JavaProcess;
import org.openimaj.util.processes.ProcessException;
import org.reflections.Reflections;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;
import uk.ac.soton.ecs.comp3204.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3204.utils.annotations.Lecture;

/**
 * Main menu interface to all the lectures and demos
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MainMenu extends JPanel {
	private static final long serialVersionUID = 1L;

	private abstract static class RunnableObject {
		Class<?> mainClass;
		JvmArgs args;

		public abstract String getTitle();

		@Override
		public String toString() {
			return getTitle();
		}

		public abstract String getAuthor();

		public JvmArgs getJvmArgs() {
			return args;
		}
	}

	private static class DemoObject extends RunnableObject {
		Demonstration demo;

		@Override
		public String getTitle() {
			return demo.title();
		}

		@Override
		public String getAuthor() {
			return demo.author();
		}
	}

	private static class LectureObject extends RunnableObject implements Comparable<LectureObject> {
		Lecture lecture;
		List<DemoObject> demos = new ArrayList<DemoObject>();

		@Override
		public String getTitle() {
			return lecture.title();
		}

		@Override
		public String getAuthor() {
			return lecture.author();
		}

		@Override
		public int compareTo(LectureObject o) {
			final Integer thisNo = Integer.parseInt((String) this.lecture.title().subSequence(1,
					this.lecture.title().indexOf(":")));
			final Integer thatNo = Integer.parseInt((String) o.lecture.title().subSequence(1,
					o.lecture.title().indexOf(":")));

			return thisNo.compareTo(thatNo);
		}
	}

	private JTabbedPane tabs;

	/**
	 * Construct the UI
	 */
	public MainMenu() {
		final List<LectureObject> lectures = getLectures();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		tabs = new JTabbedPane();
		final List<JButton> runBtns = new ArrayList<JButton>();
		for (final LectureObject l : lectures) {
			final Component lp = createLecturePanel(l, runBtns);
			tabs.addTab(l.lecture.title(), lp);
		}

		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int idx = tabs.getSelectedIndex();
				final JRootPane root = MainMenu.this.getRootPane();

				if (root != null && idx >= 0)
					root.setDefaultButton(runBtns.get(idx));
			}
		});

		add(tabs);

		final JPanel info = new JPanel(new GridLayout(0, 1));
		info.setPreferredSize(new Dimension(800, 30));
		info.setSize(info.getPreferredSize());
		info.setMaximumSize(info.getPreferredSize());

		final JLabel link = Utils.linkify("http://comp3204.ecs.soton.ac.uk", "http://comp3204.ecs.soton.ac.uk",
				"Go to the course web site");
		link.setHorizontalAlignment(SwingConstants.CENTER);
		info.add(link);

		add(info);
	}

	/**
	 * Create a tabbed panel for each lecture
	 *
	 * @param l
	 * @return the panel
	 */
	private Component createLecturePanel(LectureObject l, List<JButton> runBtns) {
		final JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2));

		final JPanel controls = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();

		final JLabel titleField = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 0;
		controls.add(titleField, gbc);

		final JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(100, 10));
		gbc.gridx = 0;
		gbc.gridy = 1;
		controls.add(spacer, gbc);

		final JLabel authorField = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 2;
		controls.add(authorField, gbc);

		final JPanel spacer2 = new JPanel();
		spacer2.setPreferredSize(new Dimension(100, 50));
		gbc.gridx = 0;
		gbc.gridy = 3;
		controls.add(spacer2, gbc);

		final JPanel details = new JPanel(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 4;
		controls.add(details, gbc);

		final JPanel spacer3 = new JPanel();
		spacer3.setPreferredSize(new Dimension(100, 100));
		gbc.gridx = 0;
		gbc.gridy = 5;
		controls.add(spacer3, gbc);

		final JButton runBtn = new JButton("Load");
		// runBtn.setFont(Font.decode(runBtn.getFont().getFontName() + "-48"));
		gbc.gridx = 0;
		gbc.gridy = 6;
		controls.add(runBtn, gbc);
		runBtns.add(runBtn);

		final DefaultListModel<RunnableObject> model = new DefaultListModel<RunnableObject>();
		model.addElement(l);
		for (final DemoObject i : l.demos)
			model.addElement(i);

		final JList<RunnableObject> list = new JList<RunnableObject>(model);
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus)
			{
				final JLabel label = (JLabel) super.getListCellRendererComponent(list,
						((RunnableObject) value).getTitle(), index, isSelected, cellHasFocus);
				// label.setIcon(icon);
				return label;
			}
		});
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					details.removeAll();

					final RunnableObject ro = (model.get(list.getSelectedIndex()));

					authorField.setText(ro.getAuthor());
					titleField.setText(ro.getTitle());

					if (ro instanceof LectureObject) {
						final LectureObject leo = (LectureObject) ro;

						final GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridy = 0;
						details.add(Utils.linkify("• View the lecture slides PDF",
								leo.lecture.slidesURL(), "View the lecture slides PDF"), gbc);

						if (!leo.lecture.handoutsURL().equals("")) {
							gbc.gridy = 1;
							details.add(Utils.linkify("• Open the handouts PDF",
									leo.lecture.handoutsURL(), "Open the handouts PDF"), gbc);
						}
						spacer2.setPreferredSize(new Dimension(100, 100 - details.getPreferredSize().height));
					} else {
						spacer2.setPreferredSize(new Dimension(100, 100));
					}
				}
			}
		});

		runBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final RunnableObject ro = list.getSelectedValue();
				try {
					MainMenu.this.runDemoOrPresentationNewJVM(ro.mainClass, ro.getJvmArgs());
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		list.setSelectedIndex(0);

		p.add(list);
		p.add(controls);

		return p;
	}

	/**
	 * Find all the lectures on the classpath
	 *
	 * @return the lectures
	 */
	private List<LectureObject> getLectures() {
		final Set<Class<?>> lectures = findLectures();
		final Set<Class<?>> demos = findDemonstrations();
		final List<LectureObject> los = new ArrayList<LectureObject>();

		for (final Class<?> c : lectures) {
			final LectureObject lo = new LectureObject();
			lo.lecture = c.getAnnotation(Lecture.class);
			lo.mainClass = c;
			lo.args = c.getAnnotation(JvmArgs.class);

			for (final Class<?> d : demos) {
				if (d.getPackage().getName().equals(lo.mainClass.getPackage().getName())) {
					final DemoObject demo = new DemoObject();
					demo.demo = d.getAnnotation(Demonstration.class);
					demo.mainClass = d;
					demo.args = c.getAnnotation(JvmArgs.class);
					lo.demos.add(demo);
				}
			}

			los.add(lo);
		}

		Collections.sort(los);

		return los;
	}

	/**
	 * Finds the class files that have been annotated with the @Lecture
	 * annotation.
	 */
	private Set<Class<?>> findLectures() {
		final Reflections reflections = new Reflections(this.getClass().getPackage().getName());
		return reflections.getTypesAnnotatedWith(Lecture.class);
	}

	/**
	 * Finds the class files that have been annotated with the @Demonstration
	 * annotation.
	 */
	private Set<Class<?>> findDemonstrations() {
		final Reflections reflections = new Reflections(this.getClass().getPackage().getName());
		return reflections.getTypesAnnotatedWith(Demonstration.class);
	}

	/**
	 * Given a presentation class file, instantiate the demo and run its main
	 * method in a new JVM
	 *
	 * @param clazz
	 *            The demo class file
	 */
	private void runDemoOrPresentationNewJVM(final Class<?> clazz, JvmArgs annotation) throws Exception {
		final String[] jvmArgs = annotation == null ? new String[0] : annotation.vmArguments();
		final String[] appArgs = annotation == null ? new String[0] : annotation.arguments();

		new Thread() {
			@Override
			public void run() {
				try {
					JavaProcess.runProcess(clazz, jvmArgs, appArgs);
				} catch (final ProcessException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	/**
	 * Run the menu app
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame f = new JFrame();
		final MainMenu mm = new MainMenu();
		f.getContentPane().add(mm);
		f.setSize(800, 600);
		f.setLocationRelativeTo(null);
		mm.tabs.setSelectedIndex(1);
		mm.tabs.setSelectedIndex(0);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
