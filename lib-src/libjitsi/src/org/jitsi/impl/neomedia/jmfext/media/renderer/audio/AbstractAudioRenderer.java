/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.impl.neomedia.jmfext.media.renderer.audio;

import java.beans.*;

import javax.media.*;
import javax.media.format.*;

import net.sf.fmj.media.util.*;

import org.jitsi.impl.neomedia.device.*;
import org.jitsi.impl.neomedia.jmfext.media.renderer.*;

/**
 * Provides an abstract base implementation of <tt>Renderer</tt> which processes
 * media in <tt>AudioFormat</tt> in order to facilitate extenders.
 *
 * @param <T> the runtime type of the <tt>AudioSystem</tt> which provides the
 * playback device used by the <tt>AbstractAudioRenderer</tt>
 *
 * @author Lyubomir Marinov
 */
public abstract class AbstractAudioRenderer<T extends AudioSystem>
    extends AbstractRenderer<AudioFormat>
{
    /**
     * The <tt>AudioSystem</tt> which provides the playback device used by this
     * <tt>Renderer</tt>.
     */
    protected final T audioSystem;

    /**
     * The flow of the media data (to be) implemented by this instance which is
     * either {@link AudioSystem.DataFlow#NOTIFY} or
     * {@link AudioSystem.DataFlow#PLAYBACK}.
     */
    protected final AudioSystem.DataFlow dataFlow;

    /**
     * The <tt>MediaLocator</tt> which specifies the playback device to be used
     * by this <tt>Renderer</tt>.
     */
    private MediaLocator locator;

    /**
     * The <tt>PropertyChangeListener</tt> which listens to changes in the
     * values of the properties of {@link #audioSystem}.
     */
    private final PropertyChangeListener propertyChangeListener
        = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent ev)
            {
                AbstractAudioRenderer.this.propertyChange(ev);
            }
        };

    /**
     * Initializes a new <tt>AbstractAudioRenderer</tt> instance which is to use
     * playback devices provided by a specific <tt>AudioSystem</tt>.
     *
     * @param audioSystem the <tt>AudioSystem</tt> which is to provide the
     * playback devices to be used by the new instance
     */
    protected AbstractAudioRenderer(T audioSystem)
    {
        this(audioSystem, AudioSystem.DataFlow.PLAYBACK);
    }

    /**
     * Initializes a new <tt>AbstractAudioRenderer</tt> instance which is to use
     * notification or playback (as determined by <tt>dataFlow</tt>) devices
     * provided by a specific <tt>AudioSystem</tt>.
     *
     * @param audioSystem the <tt>AudioSystem</tt> which is to provide the
     * notification or playback devices to be used by the new instance
     * @param dataFlow the flow of the media data to be implemented by the new
     * instance i.e. whether notification or playback devices provided by the
     * specified <tt>audioSystem</tt> are to be used by the new instance. Must
     * be either {@link AudioSystem.DataFlow#NOTIFY} or
     * {@link AudioSystem.DataFlow#PLAYBACK}.
     * @throws IllegalArgumentException if the specified <tt>dataFlow</tt> is
     * neither <tt>AudioSystem.DataFlow.NOTIFY</tt> nor
     * <tt>AudioSystem.DataFlow.PLAYBACK</tt>
     */
    protected AbstractAudioRenderer(
            T audioSystem,
            AudioSystem.DataFlow dataFlow)
    {
        if ((dataFlow != AudioSystem.DataFlow.NOTIFY)
                && (dataFlow != AudioSystem.DataFlow.PLAYBACK))
            throw new IllegalArgumentException("dataFlow");

        this.audioSystem = audioSystem;
        this.dataFlow = dataFlow;
    }

    /**
     * Initializes a new <tt>AbstractAudioRenderer</tt> instance which is to use
     * playback devices provided by an <tt>AudioSystem</tt> specified by the
     * protocol of the <tt>MediaLocator</tt>s of the <tt>CaptureDeviceInfo</tt>s
     * registered by the <tt>AudioSystem</tt>.
     *
     * @param locatorProtocol the protocol of the <tt>MediaLocator</tt>s of the
     * <tt>CaptureDeviceInfo</tt> registered by the <tt>AudioSystem</tt> which
     * is to provide the playback devices to be used by the new instance
     */
    @SuppressWarnings("unchecked")
    protected AbstractAudioRenderer(String locatorProtocol)
    {
        this((T) AudioSystem.getAudioSystem(locatorProtocol));
    }

    /**
     * {@inheritDoc}
     */
    public void close()
    {
        if (audioSystem != null)
            audioSystem.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * Gets the <tt>MediaLocator</tt> which specifies the playback device to be
     * used by this <tt>Renderer</tt>.
     *
     * @return the <tt>MediaLocator</tt> which specifies the playback device to
     * be used by this <tt>Renderer</tt>
     */
    public MediaLocator getLocator()
    {
        MediaLocator locator = this.locator;

        if ((locator == null) && (audioSystem != null))
        {
            CaptureDeviceInfo device = audioSystem.getSelectedDevice(dataFlow);

            if (device != null)
                locator = device.getLocator();
        }
        return locator;
    }

    /**
     * {@inheritDoc}
     */
    public Format[] getSupportedInputFormats()
    {
        /*
         * XXX If the AudioSystem (class) associated with this Renderer (class
         * and its instances) fails to initialize, the following may throw a
         * NullPointerException. Such a throw should be considered appropriate.
         */
        return audioSystem.getDevice(dataFlow, getLocator()).getFormats();
    }

    /**
     * {@inheritDoc}
     */
    public void open()
        throws ResourceUnavailableException
    {
        /*
         * If this Renderer has not been forced to use a playback device with a
         * specific MediaLocator, it will use the default playback device (of
         * its associated AudioSystem). In the case of using the default
         * playback device, change the playback device used by this instance
         * upon changes of the default playback device.
         */
        if ((this.locator == null) && (audioSystem != null))
        {
            MediaLocator locator = getLocator();

            if (locator != null)
                audioSystem.addPropertyChangeListener(propertyChangeListener);
        }
    }

    /**
     * Notifies this instance that the value of the property of
     * {@link #audioSystem} which identifies the default notification or
     * playback (as determined by {@link #dataFlow}) device has changed. The
     * default implementation does nothing so extenders may safely not call back
     * to their <tt>AbstractAudioRenderer</tt> super.
     *
     * @param ev a <tt>PropertyChangeEvent</tt> which specifies details about
     * the change such as the name of the property and its old and new values
     */
    protected void playbackDevicePropertyChange(PropertyChangeEvent ev)
    {
    }

    /**
     * Notifies this instance about a specific <tt>PropertyChangeEvent</tt>.
     * <tt>AbstractAudioRenderer</tt> listens to changes in the values of the
     * properties of {@link #audioSystem}
     *
     * @param ev the <tt>PropertyChangeEvent</tt> to notify this instance about
     */
    private void propertyChange(PropertyChangeEvent ev)
    {
        String propertyName;

        switch (dataFlow)
        {
        case NOTIFY:
            propertyName = NotifyDevices.PROP_DEVICE;
            break;
        case PLAYBACK:
            propertyName = PlaybackDevices.PROP_DEVICE;
            break;
        default:
            // The value of the field dataFlow is either NOTIFY or PLAYBACK.
            return;
        }
        if (propertyName.equals(ev.getPropertyName()))
            playbackDevicePropertyChange(ev);
    }

    /**
     * Sets the <tt>MediaLocator</tt> which specifies the playback device to be
     * used by this <tt>Renderer</tt>.
     *
     * @param locator the <tt>MediaLocator</tt> which specifies the playback
     * device to be used by this <tt>Renderer</tt>
     */
    public void setLocator(MediaLocator locator)
    {
        if (this.locator == null)
        {
            if (locator == null)
                return;
        }
        else if (this.locator.equals(locator))
            return;

        this.locator = locator;
    }

    /**
     * Changes the priority of the current thread to a value which is considered
     * appropriate for the purposes of audio processing. 
     */
    public static void useAudioThreadPriority()
    {
        useThreadPriority(MediaThread.getAudioPriority());
    }
}
