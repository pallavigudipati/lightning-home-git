package com.hp.home;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import edu.cmu.sphinx.api.Configuration;
import lightningHome.SpellRecognition;

/**
 * Servlet implementation class SpeechRecognizer
 */
@WebServlet("/SpeechRecognizer")
public class SpeechRecognizer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SpeechRecognizer() {
		super();
		// TODO Auto-generated constructor stub
	}
	//    RIFF data, WAVE audio, Microsoft PCM
	private AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F; // 16000 Hz
        int sampleSizeInBits = 16; // 16 bit
        int channels = 1; // mono
        boolean signed = true;
        boolean bigEndian = false;
//        Encoding encoding = Encoding.PCM_SIGNED;
        
        AudioFormat format =  new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        return format;
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
	    out.println("Hello World");
//	    AudioFormat format = getAudioFormat();
//	    out.println(format.toString());
	    Configuration configuration = SpellRecognition.getConfiguration();
	   // System.out.println(configuration.toString());
	    out.println(configuration.getDictionaryPath());
	    out.println("Configuration loaded");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
