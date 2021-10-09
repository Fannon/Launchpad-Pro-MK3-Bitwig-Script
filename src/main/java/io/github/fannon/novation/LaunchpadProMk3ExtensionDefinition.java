package io.github.fannon.novation;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

public class LaunchpadProMk3ExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("54555913-b867-4c61-8866-5e79ca63aa88");

   public LaunchpadProMk3ExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "Launchpad Pro Mk3";
   }

   @Override
   public String getAuthor()
   {
      return "Fannon";
   }

   @Override
   public String getVersion()
   {
      return "0.1";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }

   @Override
   public String getHardwareVendor()
   {
      return "Novation";
   }

   @Override
   public String getHardwareModel()
   {
      return "Launchpad Pro Mk3";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 12;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 2;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 2;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
      if (platformType == PlatformType.WINDOWS)
      {
         list.add(new String[]{"LPProMK3 MIDI", "MIDIIN2 (LPProMK3 MIDI)"}, new String[]{"LPProMK3 MIDI", "MIDIOUT2 (LPProMK3 MIDI)"});
      }
      else if (platformType == PlatformType.MAC)
      {
         // TODO: Find a good guess for the Mac names.
      }
      else if (platformType == PlatformType.LINUX)
      {
         // TODO Find better guess. Get a Linux.
         list.add(new String[]{"Launchpad Pro Mk3 MIDI 1", "Launchpad Pro Mk3 MIDI 2"}, new String[]{"Launchpad Pro Mk3 MIDI 1", "Launchpad Pro Mk3 MIDI 2"});
      }
   }

   @Override
   public LaunchpadProMk3Extension createInstance(final ControllerHost host)
   {
      return new LaunchpadProMk3Extension(this, host);
   }
}
