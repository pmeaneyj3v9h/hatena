// Copyright © 2019 VMware, INC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package rm

import (
	"context"
	"fmt"
	"github.com/edgexfoundry-holding/edgex-cli/pkg/urlclient"
	"github.com/edgexfoundry/go-mod-core-contracts/clients"
	"github.com/edgexfoundry/go-mod-core-contracts/clients/coredata"

	"github.com/edgexfoundry-holding/edgex-cli/config"
	"github.com/spf13/cobra"
)

// NewCommand return rm events command
func NewCommand() *cobra.Command {
	var cmd = &cobra.Command{
		Use:   "rm [device ID]",
		Short: "Remove events generated by given device",
		Long:  `Removes all the events generated by a device given the device's name.`,
		Run: func(cmd *cobra.Command, args []string) {

			// Checking for args
			if len(args) == 0 {
				fmt.Printf("Error: No device name provided.\n")
				return
			}

			deviceID := args[0]


			ctx, _ := context.WithCancel(context.Background())

			url := config.Conf.DataService.Protocol + "://" +
				config.Conf.DataService.Host + ":" +
				config.Conf.DataService.Port

			dc := coredata.NewEventClient(
				urlclient.New(
					ctx,
					clients.CoreDataServiceKey,
					clients.ApiEventRoute,
					15000,
					url + clients.ApiEventRoute))

			err := dc.DeleteForDevice(ctx, deviceID)

			if err != nil {
				fmt.Println(err)
				return
			}

			fmt.Println("Removing events for device:")
			fmt.Println(deviceID)

			//respBody, err := client.DeleteItemByName(deviceID,
			//	config.Conf.DataService.DeleteEventByDeviceIDRoute,
			//	config.Conf.DataService.Port)
			//
			//if err != nil {
			//	fmt.Println(err)
			//	return
			//}
			//
			//// Display Results
			//if string(respBody) == "0" {
			//	fmt.Printf("Removed events for device: %s\n", deviceID)
			//} else {
			//	fmt.Printf("Remove Unsuccessful!\n")
			//}
			//
			//fmt.Println("Removing events for device:")
			//fmt.Println(deviceID)
		},
	}
	return cmd
}
