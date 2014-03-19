package uk.ac.soton.ecs.comp3005.l5;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.util.function.Operation;

import uk.ac.soton.ecs.comp3005.utils.PixelDrawingComponent;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

@Demonstration(title = "Moment features demo")
public class MomentFeaturesDemo implements Slide, Operation<FImage>, ChangeListener {
	static Font FONT = Font.decode("Monaco-30");

	private PixelDrawingComponent pdc;

	private JTextField cenField;

	private JTextField stdField;

	private JTextField ncenField;

	private JSpinner pSpinner;

	private JSpinner qSpinner;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		pdc = new PixelDrawingComponent(400, 10);
		pdc.addActionListener(this);
		base.add(pdc);

		final JPanel rightPanel = new JPanel(new GridBagLayout());
		final JPanel controls = new JPanel();
		final JLabel pLabel = new JLabel("<html><i>p:</i></html>");
		pLabel.setFont(FONT);
		controls.add(pLabel);

		pSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
		controls.add(pSpinner);

		final JPanel sp = new JPanel();
		sp.setPreferredSize(new Dimension(20, 100));
		controls.add(sp);

		final JLabel qLabel = new JLabel("<html><i>q:</i></html>");
		qLabel.setFont(FONT);
		controls.add(qLabel);

		qSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
		controls.add(qSpinner);

		final GridBagConstraints gbc = new GridBagConstraints();
		rightPanel.add(controls, gbc);

		final JPanel features = new JPanel(new GridBagLayout());
		final JLabel stdLabel = new
				JLabel("<html><i>m<sub>pq</sub>=</i></html>");
		stdLabel.setFont(FONT);
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		features.add(stdLabel, gbc);

		stdField = new JTextField(8);
		stdField.setHorizontalAlignment(JTextField.RIGHT);
		stdField.setEditable(false);
		stdField.setFont(FONT);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		features.add(stdField, gbc);

		gbc.gridy = 1;

		final JLabel cenLabel = new
				JLabel("<html><i>μ<sub>pq</sub>=</i></html>");
		cenLabel.setFont(FONT);
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		features.add(cenLabel, gbc);

		cenField = new JTextField(8);
		cenField.setHorizontalAlignment(JTextField.RIGHT);
		cenField.setEditable(false);
		cenField.setFont(FONT);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		features.add(cenField, gbc);

		gbc.gridy = 2;

		final JLabel ncenLabel = new
				JLabel("<html><i>η<sub>pq</sub>=</i></html>");
		ncenLabel.setFont(FONT);
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		features.add(ncenLabel, gbc);

		ncenField = new JTextField(8);
		ncenField.setHorizontalAlignment(JTextField.RIGHT);
		ncenField.setEditable(false);
		ncenField.setFont(FONT);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		features.add(ncenField, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;
		rightPanel.add(features, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;
		rightPanel.add(features, gbc);

		final JPanel sp2 = new JPanel();
		sp2.setPreferredSize(new Dimension(100, 100));
		base.add(sp2);
		base.add(rightPanel);

		pSpinner.addChangeListener(this);
		qSpinner.addChangeListener(this);

		return base;
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public void perform(FImage image) {
		final List<ConnectedComponent> comps = ConnectedComponentLabeler.Algorithm.TWO_PASS.findComponents(image, 0,
				ConnectMode.CONNECT_8);

		if (comps.size() == 1) {
			final ConnectedComponent cc = comps.get(0);

			final int p = (Integer) pSpinner.getValue();
			final int q = (Integer) qSpinner.getValue();

			final double stdm = cc.calculateMoment(p, q, 0, 0);
			final double cenm = cc.calculateMoment(p, q);
			final double ncenm = cc.calculateMomentNormalised(p, q);

			this.stdField.setText(String.format("%4.3f", stdm));
			this.cenField.setText(String.format("%4.3f", cenm));
			this.ncenField.setText(String.format("%4.3f", ncenm));
		} else {
			this.stdField.setText("");
			this.cenField.setText("");
			this.ncenField.setText("");
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		this.perform(this.pdc.getImage());
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new MomentFeaturesDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}

}
