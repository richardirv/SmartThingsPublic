/**
 *  Neato Botvac Connected Series
 *
 *  Copyright 2017 Alex Lee Yuk Cheung
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  VERSION HISTORY
 *  08-10-2018: 1.10 - Initial compatibility with New Smartthings App.
 *  23-09-2018: 1.9.2b - Reduce the CAPS on Android tile labels.
 *  21-09-2018: 1.9.2 - Support for D4 and D6 models. Replace Neato logo with empty icon for unsupport feature tiles.
 *  18-04-2018: 1.9.1 - Enable methods to enable WebCORE to set botvac mode.
 *  18-04-2018: 1.9b - Incorrect Persistent Map mode label fix.
 *  18-04-2018: 1.9 - Support for D7 persistent map cleaning and deep cleaning mode.
 *	15-04-2018: 1.8.1 - Fix support for D7 with houseCleaning basic-3 support.
 *	21-12-2017: 1.8 - Add map support for D3 and D5 models with firmware V4x
 *	06-09-2017: 1.7b - D7 remove navigation mode it's not supported.
 *	06-09-2017: 1.7a - Fix support for D7 Eco/Turbo.
 *	06-09-2017: 1.7 - Add support for D5 Extra Care. Add support for D7 Eco/Turbo.
 *	06-09-2017: 1.6 - Add support for D7 including Maps and Find Me.
 *
 *  31-03-2017: 1.5.1b - Add actuator capability for ACTION TILES compatability.
 *	24-01-2017: 1.5.1 - Sq ft display on maps and stats.
 *
 *	24-01-2017: 1.5b - Better error handling for maps.
 * 	17-01-2017: 1.5 - Find Me support and stats reporting for D5. Minor tweaks to stats table formatting.
 *
 * 	12-01-2017: 1.4b - Time zones!.
 * 	12-01-2017: 1.4 - Cleaning map view functionality.
 *
 * 	13-12-2016: 1.3b - Attempt to stop Null Pointer on 1.3b.
 * 	13-12-2016: 1.3 - Added compatability with newer Botvac models with firmware 3.x.
 *
 * 	12-12-2016: 1.2.2c - Bug fix. Prevent NULL error on result.error string.
 *
 * 	01-11-2016: 1.2.2b - Bug fix. Stop disabling Neato Schedule even when SmartSchedule is off.
 *	26-10-2016: 1.2.2 - Turn off 'searching' status when Botvac is idle. More information for activity feed.
 *	
 *	25-10-2016: 1.2.1 - New device tile to change cleaning mode. Icon refactor.
 *
 *  25-10-2016: 1.2b - Very silly bug fix. Clean mode always reporting as Eco. Added display cleaning mode in Device Handler.
 *	23-10-2016: 1.2 - Add option to select Turbo or Eco clean modes
 *
 *	20-10-2016: 1.1b - Minor display tweak for offline condition.
 *	20-10-2016: 1.1 - Added smart schedule and force clean status messages. Added smart schedule reset button.
 *					  Disable Neato Robot Schedule if SmartSchedule is enabled.
 *	
 *	14-10-2016: 1.0 - Initial Version
 *
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

metadata {
	definition (name: "Neato Botvac Connected Series", namespace: "alyc100", author: "Alex Lee Yuk Cheung", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
    	capability "Battery"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
        capability "Actuator"
        capability "Health Check"
        
		command "refresh"
        command "dock"
        command "enableSchedule"
        command "disableSchedule"
        command "resetSmartSchedule"
        command "toggleCleaningMode"
        command "toggleNavigationMode"
        command "togglePersistentMapMode"
        command "setCleaningMode", ["string"]
        command "setNavigationMode", ["string"]
        command "setPersistentMapMode", ["string"]
        command "findMe"

		attribute "network","string"
		attribute "bin","string"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
    	multiAttributeTile(name: "clean", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.switch", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState("off", label: 'STOPPED', action: "switch.on", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor: "#ffffff", nextState:"on")
				attributeState("on", label: 'CLEANING', action: "switch.off", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/best-pet-hair-cleaning.png", backgroundColor: "#79b821", nextState:"off")
				attributeState("offline", label:'OFFLINE', icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor:"#bc2323")
            }
            tileAttribute ("statusMsg", key: "SECONDARY_CONTROL") {
				attributeState "statusMsg", label:'${currentValue}'
			}
		}
        valueTile("smartScheduleStatusMessage", "device.smartScheduleStatusMessage", decoration: "flat", width: 3, height: 1) {
			state "default", label: '${currentValue}'
		}
        
        valueTile("forceCleanStatusMessage", "device.forceCleanStatusMessage", decoration: "flat", width: 3, height: 1) {
			state "default", label: '${currentValue}'
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("charging", "device.charging", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
         	state ("true", label:'Charging', icon: "st.samsung.da.RC_ic_charge", backgroundColor: "#E5E500")
			state ("false", label:'', icon: "st.samsung.da.RC_ic_charge")
		}
		standardTile("bin", "device.bin", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state ("empty", label:'Bin Empty', icon: "st.Office.office10",backgroundColor: "#79b821")
			state ("full", label:'Bin Full', icon: "st.Office.office10", backgroundColor: "#bc2323")
		}
		standardTile("network", "device.network", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state ("Connected", label:'Online', icon: "st.Health & Wellness.health9", backgroundColor: "#79b821")
			state ("Not Connected", label:'Offline', icon: "st.Health & Wellness.health9", backgroundColor: "#bc2323")
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon")
		}
        standardTile("status", "device.status", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("unknown", label:'Unknown', icon: "st.unknown.unknown.unknown")
			state ("cleaning", label:'Cleaning', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/best-pet-hair-cleaning.png")
			state ("ready", label:'Ready', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png")
			state ("error", label:'Error', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor: "#bc2323")
			state ("paused", label:'Paused', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png")
		}
        
        standardTile("dockStatus", "device.dockStatus", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
         	state ("docked", label:'Docked', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/auto-charge-resume.png")
			state ("dockable", label:'Dock', action: "dock", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_staub.png")
            state ("undocked", label:'Undocked', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png")
		}
        
        standardTile("scheduled", "device.scheduled", width: 2, height: 2, decoration: "flat") {
         	state ("true", label:'Neato Sched On', action: "disableSchedule", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_schedule_icon.png")
			state ("false", label:'Neato Sched Off', action: "enableSchedule", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_no_schedule_icon.png")
		}
        
        standardTile("dockHasBeenSeen", "device.dockHasBeenSeen", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
         	state ("true", label:'Seen', backgroundColor: "#79b821", icon:"st.Transportation.transportation13")
			state ("false", label:'Searching', backgroundColor: "#E5E500", icon:"st.Transportation.transportation13")
            state ("idle", label:'', icon:"st.Transportation.transportation13")
		}
        
        standardTile("resetSmartSchedule", "device.resetSmartSchedule", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state("default", label:'reset schedule', action:"resetSmartSchedule", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/reset_schedule_icon.png")
		}
        
        standardTile("cleaningMode", "device.cleaningMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state("turbo", label:'Turbo Mode', action:"toggleCleaningMode", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_turbo_icon.png")
            state("eco", label:'Eco Mode', action:"toggleCleaningMode", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_eco_icon.png")
            state("findMe", label:'Find Me', action:"findMe", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_findme_icon.png")
            state("empty", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/empty.png")
		}
        
        standardTile("navigationMode", "device.navigationMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state("standard", label:'Standard', action:"toggleNavigationMode", icon:"st.Appliances.appliances13")
            state("extraCare", label:'Extra Care', action:"toggleNavigationMode", icon:"st.Outdoor.outdoor1")
            state("deep", label:'Deep', action:"toggleNavigationMode", icon:"st.Bath.bath13")
            state("findMe", label:'Find Me', action:"findMe", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_findme_icon.png")
            state("empty", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/empty.png")
		}
        
        standardTile("persistentMapMode", "device.persistentMapMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state("off", label:'Pers Map Off', action:"togglePersistentMapMode", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_floor_icon.png")
            state("on", label:'Pers Map On', action:"togglePersistentMapMode", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_floor_icon.png")
            state("empty", icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/empty.png")
		}
        
        standardTile("switch", "device.switch", width: 2, height: 2, decoration: "flat") {
        	state("off", label: 'STOPPED', action: "switch.on", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor: "#ffffff", nextState:"on")
			state("on", label: 'CLEANING', action: "switch.off", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/best-pet-hair-cleaning.png", backgroundColor: "#79b821", nextState:"off")
			state("offline", label:'${name}', icon:"https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor:"#bc2323")
        }
        
        htmlTile(name:"mapHTML", action: "getMapHTML", width: 6, height: 9, whiteList: ["neatorobotics.s3.amazonaws.com", "raw.githubusercontent.com"])
        
		main("switch")
		details(["clean","smartScheduleStatusMessage", "forceCleanStatusMessage", "status", "battery", "charging", "bin", "dockStatus", "dockHasBeenSeen", "cleaningMode", "navigationMode", "persistentMapMode", "scheduled", "resetSmartSchedule", "network", "refresh", "mapHTML"])
	}
}

mappings {
    path("/getMapHTML") {action: [GET: "getMapHTML"]}
}

// handle commands

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
    sendEvent(name: "checkInterval", value: 10 * 60 + 2 * 60, data: [protocol: "cloud"], displayed: false)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
    sendEvent(name: "checkInterval", value: 10 * 60 + 2 * 60, data: [protocol: "cloud"], displayed: false)
}

def initialize() {
	if (state.startCleaningMode == null) {
    	state.startCleaningMode = "turbo"
        sendEvent(name: 'cleaningMode', value: state.startCleaningMode, displayed: true)
    }
    if (state.startNavigationMode == null) {
    	state.startNavigationMode = "standard"
        sendEvent(name: 'navigationMode', value: state.startNavigationMode, displayed: true)
    }
    if (state.startPersistentMapMode == null) {
    	state.startPersistentMapMode = "off"
        sendEvent(name: 'persistentMapMode', value: state.startPersistentMapMode, displayed: true)
    }
	poll()
}

def on() {
	log.debug "Executing 'on'"
    def currentState = device.latestState('status').stringValue
    if (currentState == 'paused') {
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"resumeCleaning"}')
    }
    else if (currentState != 'error') {
    	def modeParam = 1
        def navParam = 1
        def catParam = 2
        if (isTurboCleanMode()) modeParam = 2
        if (isExtraCareNavigationMode()) navParam = 2
        if (isDeepNavigationMode()) {
        	modeParam = 2
            navParam = 3
        }
        if (isPersistentMapMode()) catParam = 4
        switch (state.houseCleaning) {
            case "basic-1":
               	nucleoPOST("/messages", '{"reqId":"1", "cmd":"startCleaning", "params":{"category": 2, "mode": ' + modeParam + ', "modifier": 1}}')
			break;
			case "minimal-2":
				nucleoPOST("/messages", '{"reqId":"1", "cmd":"startCleaning", "params":{"category": 2, "navigationMode": ' + navParam + '}}')
			break;
            default:
            	nucleoPOST("/messages", '{"reqId":"1", "cmd":"startCleaning", "params":{"category": ' + catParam + ', "mode": ' + modeParam + ', "navigationMode": ' + navParam + '}}')
            break;
        }
    }
    runIn(2, refresh)
}

def off() {
	log.debug "Executing 'off'"
    def currentState = device.latestState('status').stringValue
    if (currentState == 'cleaning' || currentState == 'error') {
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"pauseCleaning"}')
    }
    runIn(2, refresh)
}

def dock() {
	log.debug "Executing 'dock'"
    if (device.latestState('status').stringValue == 'paused') {
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"sendToBase"}')
    }
    runIn(2, refresh)
}

def enableSchedule() {
	log.debug "Executing 'enableSchedule'"
	nucleoPOST("/messages", '{"reqId":"1", "cmd":"enableSchedule"}')
    runIn(2, refresh)
}

def disableSchedule() {
	log.debug "Executing 'disableSchedule'"
	nucleoPOST("/messages", '{"reqId":"1", "cmd":"disableSchedule"}')
    runIn(2, refresh)
}

def findMe() {
	log.debug "Executing 'findMe'"
    nucleoPOST("/messages", '{"reqId":"1", "cmd": "findMe"}')
}

def setOffline() {
	sendEvent(name: 'network', value: "Not Connected" as String)
    sendEvent(name: "switch", value: "offline")
}

def resetSmartSchedule() {
	log.debug "Executing 'resetSmartSchedule'"
	parent.resetSmartScheduleForDevice(device.deviceNetworkId) 
    runIn(2, refresh)
}

def toggleCleaningMode() {
	log.debug "Executing 'toggleCleaningMode'"
	if (state.startCleaningMode != null && state.startCleaningMode == "turbo") { 
    	state.startCleaningMode = "eco"
    } else {
    	state.startCleaningMode = "turbo"
    }
    sendEvent(name: 'cleaningMode', value: state.startCleaningMode, displayed: true)
    runIn(2, refresh)
}

def toggleNavigationMode() {
	log.debug "Executing 'toggleNavigationMode'"
	if (state.startNavigationMode != null && state.startNavigationMode == "standard") { 
    	state.startNavigationMode = "extraCare"
    } else if (state.startNavigationMode != null && state.startNavigationMode == "extraCare" && state.modelName == "BotVacD7Connected") {
    	state.startNavigationMode = "deep"
    } else {
    	state.startNavigationMode = "standard"
    }
    sendEvent(name: 'navigationMode', value: state.startNavigationMode, displayed: true)
    runIn(2, refresh)
}

def togglePersistentMapMode() {
	log.debug "Executing 'togglePersistentMapMode'"
	if (state.startPersistentMapMode != null && state.startPersistentMapMode == "on") { 
    	state.startPersistentMapMode = "off"
    } else {
    	state.startPersistentMapMode = "on"
    }
    sendEvent(name: 'persistentMapMode', value: state.startPersistentMapMode, displayed: true)
    runIn(2, refresh)
}

//Methods to support WebCORE mode set
def setCleaningMode(mode) {
	if ( mode == "eco" || mode == "turbo" ) {
    	state.startCleaningMode = mode
    	sendEvent(name: 'cleaningMode', value: state.startCleaningMode, displayed: true)
    } else {
    	log.error("Unsupported cleaning mode: [${mode}]")
    }
}

def setNavigationMode(mode) {
	if ( mode == "deep" || mode == "extraCare" || mode == "standard") {
    	state.startNavigationMode = mode
		sendEvent(name: 'navigationMode', value: state.startNavigationMode, displayed: true)
	} else {
    	log.error("Unsupported navigation mode: [${mode}]")
    }
}

def setPersistentMapMode(mode) {
	if ( mode == "on" || mode == "off" ) {
    	state.startPersistentMapMode = mode
		sendEvent(name: 'persistentMapMode', value: state.startPersistentMapMode, displayed: true)
	} else {
    	log.error("Unsupported persistent map mode: [${mode}]")
    }
}

def poll() {
	log.debug "Executing 'poll'"
    def resp = nucleoPOST("/messages", '{"reqId":"1", "cmd":"getRobotState"}')
    def result = resp.data
    def statusMsg = ""
    def binFullFlag = false
	if (resp.status != 200) {
    	if (result.containsKey("message")) {
        	switch (result.message) {
            	case "Could not find robot_serial for specified vendor_name":
                	statusMsg += 'Robot serial and/or secret is not correct.\n'
                break;
            }
        }
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
        setOffline()
        sendEvent(name: 'status', value: "error" as String)
        statusMsg += 'Not Connected To Neato'
		log.debug headerString
	}
    else {
    	if (result.containsKey("meta")) {
        	state.firmware = result.meta.firmware
            state.modelName = result.meta.modelName
        }
        if (result.containsKey("availableServices")) {
        	state.houseCleaning = result.availableServices.houseCleaning
        }
        if (result.containsKey("state")) {
        	sendEvent(name: 'network', value: "Connected" as String)
        	//state 1 - Ready to clean
        	//state 2 - Cleaning
        	//state 3 - Paused
       		//state 4 - Error
            switch (result.state) {
        		case "1":
            		sendEvent(name: "status", value: "ready")
                	sendEvent(name: "switch", value: "off")
                    if (state.firmware.startsWith("2")) {
                   		statusMsg += "READY TO ${isTurboCleanMode() ? "TURBO" : "ECO"} CLEAN"
                    } else {
                    	statusMsg += "READY TO CLEAN"
                    }
				break;
				case "2":
					sendEvent(name: "status", value: "cleaning")
                	sendEvent(name: "switch", value: "on")
                    if (state.firmware.startsWith("2")) {
                		statusMsg += "CURRENTLY ${isTurboCleanMode() ? "TURBO" : "ECO"} CLEANING"
                    } else {
                    	statusMsg += "CURRENTLY CLEANING"
                    }
				break;
            	case "3":
					sendEvent(name: "status", value: "paused")
                	sendEvent(name: "switch", value: "off")
                	statusMsg += 'PAUSED'
                    def t = parent.autoDockDelayValue()
                    if (t != -1) { statusMsg += " - Auto dock set to $t seconds." }
				break;
            	case "4":
					sendEvent(name: "status", value: "error")
                	statusMsg += 'HAS A PROBLEM'
				break;
            	default:
               		sendEvent(name: "status", value: "unknown")
                	statusMsg += 'UNKNOWN'
				break;
        	}
        }
        if (state.firmware.startsWith("2") && result.containsKey("error")) {
        	switch (result.error) {
            	case "ui_alert_dust_bin_full":
					binFullFlag = true
				break;
                case "ui_alert_return_to_base":
					statusMsg += ' - Returning to base'
				break;
                case "ui_alert_return_to_start":
					statusMsg += ' - Returning to start'
				break;
                case "ui_alert_return_to_charge":
					statusMsg += ' - Returning to charge'
				break;
                case "ui_alert_busy_charging":
					statusMsg += ' - Busy charging'
				break;
                case "ui_alert_recovering_location":
					statusMsg += ' - Recovering Location'
				break;
                case "ui_error_dust_bin_full":
					binFullFlag = true
                    statusMsg += ' - Dust bin full!'
				break;
            	case "ui_error_picked_up":
					statusMsg += ' - Picked up!'
				break;
                case "ui_error_brush_stuck":
                	statusMsg += ' - Brush stuck!'
                break;
                case "ui_error_stuck":
                	statusMsg += ' - I\'m stuck!'
                break;
                case "ui_error_dust_bin_missing":
                	statusMsg += ' - Dust Bin is missing!'
                break
                case "ui_error_navigation_falling":
                	statusMsg += ' - Please clear my path!'
                break
                case "ui_error_navigation_noprogress":
                	statusMsg += ' - Please clear my path!'
                break
                case "ui_error_battery_overtemp":
                	statusMsg += ' - Battery is overheating!'
                break
                case "ui_error_unable_to_return_to_base":
                	statusMsg += ' - Unable to return to base!'
                break
                case "ui_error_bumper_stuck":
                	statusMsg += ' - Bumper stuck!'
                break
                case "ui_error_lwheel_stuck":
                	statusMsg += ' - Left wheel stuck!'
                break
                case "ui_error_rwheel_stuck":
                	statusMsg += ' - Right wheel stuck!'
                break
                case "ui_error_lds_jammed":
                	statusMsg += ' - LIDAR jammed!'
                break
                case "ui_error_brush_overload":
                	statusMsg += ' - Brush overloaded!'
                break
                case "ui_error_hardware_failure":
                	statusMsg += ' - Hardware Failure!'
                break
                case "ui_error_unable_to_see":
                	statusMsg += ' - Unable to see!'
                break
                case "ui_error_rdrop_stuck":
                	statusMsg += ' - Right drop stuck!'
                break
                case "ui_error_ldrop_stuck":
                	statusMsg += ' - Left drop stuck!'
                break
                default:
                	if ("ui_alert_invalid" != result.error) {
                		statusMsg += ' - ' + result.error.replaceAll('ui_error_', '').replaceAll('ui_alert_', '').replaceAll('_',' ').capitalize()
                    }
				break;
                //More error detail messages here as discovered
			}
        }
        if (state.firmware.startsWith("3") && result.containsKey("error")) {
        	if (result.error) {
            	if (result.error == "dustbin_full") { 
                	binFullFlag = true
                    statusMsg += ' - Dust bin full!'
                }
            	else { 
                	statusMsg += ' - ' + result.error.replaceAll('_',' ').capitalize() 
               	}
            }
        }
        if (state.firmware.startsWith("3") && result.containsKey("alert")) {
        	if (result.alert) {
            	if (result.alert == "dustbin_full") { 
                	binFullFlag = true 
                }
            }
        }
        
        //Tile configuration for models
        if (state.modelName == "BotVacD7Connected" || state.modelName == "BotVacD6Connected" || state.modelName == "BotVacD4Connected" ) {
        	//Neato Botvac D7, D6 and D4
        } else if (state.modelName == "BotVacD5Connected") {
        	//Neato Botvac D5
        	sendEvent(name: 'cleaningMode', value: "findMe", displayed: false)
            sendEvent(name: 'persistentMapMode', value: "empty", displayed: false)
        } else if (state.modelName == "BotVacD3Connected") {
        	//Neato Botvac D3
        	sendEvent(name: 'cleaningMode', value: "empty", displayed: false)
            sendEvent(name: 'persistentMapMode', value: "empty", displayed: false)
        } else {
        	//Neato Botvac Connected
            sendEvent(name: 'navigationMode', value: "empty", displayed: false)
            sendEvent(name: 'persistentMapMode', value: "empty", displayed: false)
        }
        
        if (result.containsKey("details")) {
        	if (result.details.isDocked) {
            	sendEvent(name: 'dockStatus', value: "docked" as String)
            } else {
            	sendEvent(name: 'dockStatus', value: "undocked" as String)
            }
        	sendEvent(name: 'charging', value: result.details.isCharging as String)
        	sendEvent(name: 'scheduled', value: result.details.isScheduleEnabled as String)
        	//If Botvac is idle, set dock has been seen status to idle and ignore API result
            if (result.state as String == "1") {
            	sendEvent(name: 'dockHasBeenSeen', value: "idle", displayed: false)
            } else {
            	sendEvent(name: 'dockHasBeenSeen', value: result.details.dockHasBeenSeen as String)
            }
        	sendEvent(name: 'battery', value: result.details.charge as Integer)
        }
        if (result.containsKey("availableCommands")) {
        	if (result.availableCommands.goToBase) {
        		sendEvent(name: 'dockStatus', value: "dockable")
            }
        }
        if (binFullFlag) {
        	sendEvent(name: 'bin', value: "full" as String)
        } else {
        	sendEvent(name: 'bin', value: "empty" as String)
        }
        def smartScheduleStatus = ""
        def t = parent.timeToSmartScheduleClean(device.deviceNetworkId)
        if (t != -1) {
        	if (t >= 86400000) {
            	smartScheduleStatus += "SmartSchedule activating in ${Math.round(new BigDecimal(t/86400000)).toString()} days."
            } else if ((t >= 0) && (t <= 86400000)) {
            	smartScheduleStatus += "SmartSchedule activating in ${Math.round(new BigDecimal(t/3600000)).toString()} hours."
            } else {
            	smartScheduleStatus += "SmartSchedule waiting for configured trigger."
            }
         } else {
         	smartScheduleStatus += "SmartSchedule is disabled. Configure in Neato (Connect) smart app."
         }
         def forceCleanStatus = ""
         t = parent.timeToForceClean(device.deviceNetworkId)
         if (t != -1) {
                if (t >= 86400000) {
                	forceCleanStatus += "Force clean due in ${Math.round(new BigDecimal(t/86400000)).toString()} days."
                } else if ((t >= 0) && (t <= 86400000)) {
                	forceCleanStatus += "Force clean due in ${Math.round(new BigDecimal(t/3600000)).toString()} hours."
                } else {
                	forceCleanStatus += "Force clean imminent."
             }
         } else {
         	forceCleanStatus += "Force clean is disabled. Configure in Neato (Connect) smart app."
         }
         sendEvent(name: 'smartScheduleStatusMessage', value: smartScheduleStatus, displayed: false)
         sendEvent(name: 'forceCleanStatusMessage', value: forceCleanStatus, displayed: false)
    }
    sendEvent(name: 'statusMsg', value: statusMsg, displayed: false)
    
    //Create verbose updates on activity feed.
    def statusMsgTokenized = statusMsg.tokenize('-')
    if (statusMsgTokenized.size() > 1) {
    	 def infoMsg = statusMsg.tokenize('-')[statusMsgTokenized.size() - 1].substring(1)
         sendEvent(name: "botvacInfo", value: "${infoMsg}", displayed: true, linkText: "${device.displayName}", descriptionText: "${infoMsg}")
    }
    
    //If smart schedule is enabled, disable Neato schedule to avoid conflict
    if (parent.isSmartScheduleEnabled() && result.details.isScheduleEnabled) {
    	log.debug "Disable Neato scheduling system as SmartSchedule is enabled"
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"disableSchedule"}')
        sendEvent(name: 'scheduled', value: "false")
    }
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}

private def isTurboCleanMode() {
	def result = true
    if (state.startCleaningMode != null && state.startCleaningMode == "eco") {
    	result = false
    }
    result
}

private def isExtraCareNavigationMode() {
	def result = false
    if (state.startNavigationMode != null && state.startNavigationMode == "extraCare") {
    	result = true
    }
    result
}

private def isDeepNavigationMode() {
	def result = false
    if (state.startNavigationMode != null && state.startNavigationMode == "deep") {
    	result = true
    }
    result
}

private def isPersistentMapMode() {
	def result = false
    if (state.startPersistentMapMode != null && state.startPersistentMapMode == "on") {
    	result = true
    }
    result
}

def nucleoPOST(path, body) {
	try {
		log.debug("Beginning API POST: ${nucleoURL(path)}, ${body}")
		def date = new Date().format("EEE, dd MMM yyyy HH:mm:ss z", TimeZone.getTimeZone('GMT'))
		httpPostJson(uri: nucleoURL(path), body: body, headers: nucleoRequestHeaders(date, getHMACSignature(date, body)) ) {response ->
			parent.logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		parent.logResponse(e.response)
		return e.response
	}
}

def getHMACSignature(date, body) {
	//request params
	def robot_serial = device.deviceNetworkId.tokenize("|")[0]
    //Format date should be "Fri, 03 Apr 2015 09:12:31 GMT"
	
	def robot_secret_key = device.deviceNetworkId.tokenize("|")[1]

	// build string to be signed
	def string_to_sign = "${robot_serial.toLowerCase()}\n${date}\n${body}"

	// create signature with SHA256
	//signature = OpenSSL::HMAC.hexdigest('sha256', robot_secret_key, string_to_sign)
    try {
    	Mac mac = Mac.getInstance("HmacSHA256")
    	SecretKeySpec secretKeySpec = new SecretKeySpec(robot_secret_key.getBytes(), "HmacSHA256")
    	mac.init(secretKeySpec)
    	byte[] digest = mac.doFinal(string_to_sign.getBytes())
    	return digest.encodeHex()
   	} catch (InvalidKeyException e) {
    	throw new RuntimeException("Invalid key exception while converting to HMac SHA256")
  	}
}

Map nucleoRequestHeaders(date, HMACsignature) {
	return [
        'X-Date': "${date}",
        'Accept': 'application/vnd.neato.nucleo.v1',
        'Content-Type': 'application/*+json',
        'X-Agent': '0.11.3-142',
        'Authorization': "NEATOAPP ${HMACsignature}"
    ]
}

def getMapHTML() {
	try {
    	def df = new java.text.SimpleDateFormat("MMM d, yyyy h:mm a")
		if (parent.getTimeZone()) { df.setTimeZone(location.timeZone) }
    	def resp
        def hData = ""
        if ((state.modelName == "BotVacD7Connected") || (state.firmware.startsWith("2.2")) || (state.firmware.startsWith("4"))) {
        	resp = parent.beehiveGET("/users/me/robots/${device.deviceNetworkId.tokenize("|")[0]}/maps")
            if (resp.status == 403) {
            	hData = """
                <div class="centerText" style="font-family: helvetica, arial, sans-serif;">
				  <p>Neato (Connect) not authorised for Map access.</p>
				  <p>You may need to reauthorize your Neato credentials. Open the Neato(Connect) smart app in the ST mobile app, scroll to the bottom and tap the reauthorize item.</p>
				</div>
                """
            } else if (resp.status != 200) {
            	hData = """
                <div class="centerText" style="font-family: helvetica, arial, sans-serif;">
				  <p>Neato map retrieval failed.</p>
				  <p>Please try again later.</p>
				</div>
                """
            } else if ((resp.data.maps) && (resp.data.maps.size() > 0)) {
            	def mapUrl = resp.data.maps[0].url
                def generated_at = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", resp.data.maps[0].generated_at)
                def cleaned_area = resp.data.maps[0].cleaned_area
                def start_at = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", resp.data.maps[0].start_at)
                def end_at = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", resp.data.maps[0].end_at)
				hData = """
            	<h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Cleaning Map ${df.format(generated_at)}</h4>
	  			<div id="cleaning_map" style="width: 100%"><img src="${mapUrl}" width="100%">	
                <table>
					<col width="50%">
					<col width="50%">
					<thead>
						<th>Area Cleaned</th>
						<th>Cleaning Time</th>
					</thead>
					<tbody>
						<tr>
							<td>${Math.round(cleaned_area * 100) / 100} m² / ${Math.round(cleaned_area * 1076.39) / 100} ft²</td>
							<td>${getCleaningTime(start_at, end_at)} hours</td>
						</tr>
					</tbody>
				</table>
                <table>
					<col width="50%">
					<col width="50%">
					<thead>
						<th>Status</th>
						<th>Launched From</th>
					</thead>
					<tbody>
						<tr>
							<td>${resp.data.maps[0].status.capitalize()}</td>
							<td>${resp.data.maps[0].launched_from.capitalize()}</td>
						</tr>
					</tbody>
				</table>
                </div>
				"""
            } else {
            	hData = """
                <div class="centerText" style="font-family: helvetica, arial, sans-serif;">
				  <p>No map available yet.</p>
				  <p>Complete at least one house cleaning to view maps</p>
				</div>
            """
            }
        } else if (state.modelName == "BotVacD5Connected"){
        	resp = nucleoPOST("/messages", '{"reqId":"1", "cmd":"getLocalStats"}')
            def cleaned_area = resp.data.data.houseCleaning.history[0].area
            def start_at = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'",  resp.data.data.houseCleaning.history[0].start)
            def end_at = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'",  resp.data.data.houseCleaning.history[0].end)
        	hData = """
            	<h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Statistics ${df.format(end_at)}</h4>
                <div class="centerText" style="font-family: helvetica, arial, sans-serif;">
                  <p>Please update your Botvac firmware via the Neato mobile app to get cleaning map functionality.</p>
				</div>
                <table>
					<col width="50%">
					<col width="50%">
					<thead>
						<th>Area Cleaned</th>
						<th>Cleaning Time</th>
					</thead>
					<tbody>
						<tr>
							<td>${Math.round(cleaned_area * 100) / 100} m² / ${Math.round(cleaned_area * 1076.39) / 100} ft²</td>
							<td>${getCleaningTime(start_at, end_at)} hours</td>
						</tr>
					</tbody>
				</table>
                <table>
					<col width="50%">
					<col width="50%">
					<thead>
						<th>Completed</th>
						<th>Launched From</th>
					</thead>
					<tbody>
						<tr>
							<td>${resp.data.data.houseCleaning.history[0].completed ? "Yes" : "No"}</td>
							<td>${resp.data.data.houseCleaning.history[0].launchedFrom.capitalize()}</td>
						</tr>
					</tbody>
				</table><br/>
                <h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Lifetime Statistics</h4>
				<table>
					<col width="50%">
					<col width="50%">
					<thead>
						<th>Total Cleaned Area</th>
						<th>Average Cleaned Area</th>
					</thead>
					<tbody>
						<tr>
							<td>${Math.round(resp.data.data.houseCleaning.totalCleanedArea * 100) / 100} m² / ${Math.round(resp.data.data.houseCleaning.totalCleanedArea * 1076.39) / 100} ft²</td>
							<td>${Math.round(resp.data.data.houseCleaning.averageCleanedArea * 100) / 100} m² / ${Math.round(resp.data.data.houseCleaning.averageCleanedArea * 1076.39) / 100} ft²</td>
						</tr>
					</tbody>
				</table>
                <table>
					<col width="50%">
					<col width="50%">
					<thead>
						<th>Total Cleaning Time</th>
						<th>Average Cleaning Time</th>
					</thead>
					<tbody>
						<tr>
							<td>${convertSecondsToTime(resp.data.data.houseCleaning.totalCleaningTime)} hours</td>
							<td>${convertSecondsToTime(resp.data.data.houseCleaning.averageCleaningTime)} hours</td>
						</tr>
					</tbody>
				</table>
				"""
        } else if (state.firmware.startsWith("2")) {
        	hData = """
            	<h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Update Firmware</h4>
            	<div class="centerText" style="font-family: helvetica, arial, sans-serif;">
				  <p>Cleaning maps only supported on Neato Botvac Connected models with firmware v2.2.0 or later.</p>
				  <p>If you have Neato Botvac Connected, ensure you update firmware to v2.2.0 or later.</p>
				</div>
            """
        } else {
        	hData = """
            	<h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Neato Botvac D3 Connected</h4>
            	<div class="centerText" style="font-family: helvetica, arial, sans-serif;">
                  <p>Please update your Botvac firmware via the Neato mobile app to get cleaning map functionality.</p>
				</div>
            """
        }

		def mainHtml = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta http-equiv="cache-control" content="max-age=0"/>
				<meta http-equiv="cache-control" content="no-cache"/>
				<meta http-equiv="expires" content="0"/>
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">
                <link rel="stylesheet prefetch" href="${getCssData()}"/>
			</head>
			<body>
                ${hData}
			</body>
			</html>
		"""
		render contentType: "text/html", data: mainHtml, status: 200
	}
	catch (ex) {
		log.error "getMapHTML Exception:", ex
	}
}

//Helper methods
private def getCleaningTime(start_at, end_at) {
	def diff = end_at.getTime() - start_at.getTime()
	def hour = (diff / 3600000) as Integer
    def minute = Math.round((diff - (hour * 3600000)) / 60000) as Integer
    
    def hourString = (hour < 10) ? "0$hour" : "$hour"
    def minuteString = (minute < 10) ? "0$minute" : "$minute"
    
	return "${hourString}:${minuteString}"
}

private def convertSecondsToTime(seconds) {
	def hour = (seconds / 3600) as Integer
    def minute = (seconds - (hour * 3600)) / 60 as Integer
    
    def hourString = (hour < 10) ? "0$hour" : "$hour"
    def minuteString = (minute < 10) ? "0$minute" : "$minute"
    
	return "${hourString}:${minuteString}"
}

private def getCssData() {
	def cssData = null
	def htmlInfo
	state.cssData = null

	if(htmlInfo?.cssUrl && htmlInfo?.cssVer) {
		if(state?.cssData) {
			if (state?.cssVer?.toInteger() == htmlInfo?.cssVer?.toInteger()) {
				cssData = state?.cssData
			} else if (state?.cssVer?.toInteger() < htmlInfo?.cssVer?.toInteger()) {
				cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
				state.cssData = cssData
				state?.cssVer = htmlInfo?.cssVer
			}
		} else {
			cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
			state?.cssData = cssData
			state?.cssVer = htmlInfo?.cssVer
		}
	} else {
		cssData = getFileBase64(cssUrl(), "text", "css")
	}
	return cssData
}

private def getFileBase64(url, preType, fileType) {
	try {
		def params = [
			uri: url,
			contentType: '$preType/$fileType'
		]
		httpGet(params) { resp ->
			if(resp.data) {
				def respData = resp?.data
				ByteArrayOutputStream bos = new ByteArrayOutputStream()
				int len
				int size = 4096
				byte[] buf = new byte[size]
				while ((len = respData.read(buf, 0, size)) != -1)
					bos.write(buf, 0, len)
				buf = bos.toByteArray()
				String s = buf?.encodeBase64()
				return s ? "data:${preType}/${fileType};base64,${s.toString()}" : null
			}
		}
	}
	catch (ex) {
		log.error "getFileBase64 Exception:", ex
	}
}

def cssUrl()	 { return "https://raw.githubusercontent.com/desertblade/ST-HTMLTile-Framework/master/css/smartthings.css" }
def nucleoURL(path = '/') 			 { return "https://nucleo.neatocloud.com:4443/vendors/neato/robots/${device.deviceNetworkId.tokenize("|")[0]}${path}" }