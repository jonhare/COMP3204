/**
 * Copyright (c) 2013, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.soton.ecs.comp3005.l8;

import java.awt.Component;
import java.io.IOException;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioGrabberListener;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.SampleChunk;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.vis.audio.AudioSpectrogram;

import uk.ac.soton.ecs.comp3005.utils.Utils;

public class SpectrogramDemo implements Slide, AudioGrabberListener {
	private AudioSpectrogram spectrogram;
	private JavaSoundAudioGrabber grabber;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		spectrogram = new AudioSpectrogram(width, height);

		grabber = new JavaSoundAudioGrabber(new AudioFormat(16, 11.025, 1));
		grabber.addAudioGrabberListener(this);
		grabber.setMaxBufferSize(128);
		new Thread(grabber).start();

		while (grabber.isStopped()) {
			try {
				Thread.sleep(50);
			} catch (final InterruptedException e) {
			}
		}

		return spectrogram;
	}

	@Override
	public void close() {
		if (grabber != null) {
			grabber.stop();
			grabber = null;
		}
	}

	@Override
	public void samplesAvailable(SampleChunk s) {
		spectrogram.setData(s);
		spectrogram.update();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new SpectrogramDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
