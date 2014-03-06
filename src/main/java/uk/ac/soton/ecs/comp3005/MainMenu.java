package uk.ac.soton.ecs.comp3005;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openimaj.util.processes.JavaProcess;
import org.openimaj.util.processes.ProcessException;
import org.reflections.Reflections;

import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

public class MainMenu extends JPanel {

	private abstract static class RunnableObject {
		Class<?> mainClass;

		abstract String getTitle();

		@Override
		public String toString() {
			return getTitle();
		}
	}

	private static class DemoObject extends RunnableObject {
		Demonstration demo;

		@Override
		String getTitle() {
			return demo.title();
		}
	}

	private static class LectureObject extends RunnableObject {
		Lecture lecture;
		List<DemoObject> demos = new ArrayList<DemoObject>();

		@Override
		String getTitle() {
			return lecture.title();
		}
	}

	public MainMenu() {
		final List<LectureObject> lectures = getLectures();

		this.setLayout(new GridLayout(1, 1));
		final JTabbedPane tabs = new JTabbedPane();
		for (final LectureObject l : lectures) {
			tabs.addTab(l.lecture.title(), createLecturePanel(l));
		}
		add(tabs);
	}

	private Component createLecturePanel(LectureObject l) {
		final JPanel p = new JPanel();

		final DefaultListModel model = new DefaultListModel();
		model.addElement(l);
		for (final DemoObject i : l.demos)
			model.addElement(i);

		final JList list = new JList(model);
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus)
			{
				final JLabel label = (JLabel) super.getListCellRendererComponent(list,
						((RunnableObject) value).getTitle(), index, isSelected, cellHasFocus);
				// label.setIcon(icon);
				return label;
			}
		});

		p.add(list);

		return p;
	}

	private List<LectureObject> getLectures() {
		final Set<Class<?>> lectures = findLectures();
		final Set<Class<?>> demos = findDemonstrations();
		final List<LectureObject> los = new ArrayList<LectureObject>();

		for (final Class<?> c : lectures) {
			final LectureObject lo = new LectureObject();
			lo.lecture = c.getAnnotation(Lecture.class);
			lo.mainClass = c;

			for (final Class<?> d : demos) {
				if (d.getPackage().getName().equals(lo.mainClass.getPackage().getName())) {
					final DemoObject demo = new DemoObject();
					demo.demo = d.getAnnotation(Demonstration.class);
					demo.mainClass = d;
					lo.demos.add(demo);
				}
			}

			los.add(lo);
		}

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

	public static void main(String[] args) {
		final JFrame f = new JFrame();
		f.getContentPane().add(new MainMenu());
		f.setSize(800, 600);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
