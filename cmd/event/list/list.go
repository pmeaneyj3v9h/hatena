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

package list

import (
	"context"
	"fmt"
	"io"
	"text/tabwriter"

	"github.com/edgexfoundry-holding/edgex-cli/config"
	"github.com/edgexfoundry-holding/edgex-cli/pkg/utils"

	"github.com/edgexfoundry/go-mod-core-contracts/clients"
	"github.com/edgexfoundry/go-mod-core-contracts/clients/coredata"
	"github.com/edgexfoundry/go-mod-core-contracts/clients/urlclient/local"
	"github.com/edgexfoundry/go-mod-core-contracts/models"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var limit int
var device string

// NewCommand returns the list device command
func NewCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "list",
		Short: "A list of Events",
		Long:  `Return list of Events.`,
		Args:  cobra.MaximumNArgs(1),
		RunE:  listHandler,
	}
	cmd.Flags().IntVarP(&limit, "limit", "l", 50, "Limit number of results")
	cmd.Flags().StringVarP(&device, "device", "d", "", "Events generated by specific device with given name.")
	return cmd
}

func listHandler(cmd *cobra.Command, args []string) (err error) {
	client := coredata.NewEventClient(
		local.New(config.Conf.Clients["CoreData"].Url() + clients.ApiEventRoute),
	)

	var events []models.Event
	if device != "" {
		events, err = client.EventsForDevice(context.Background(), device, limit)
	} else if len(args) > 0 {
		events, err = getEvent(client, args[0])
	} else {
		events, err = client.Events(context.Background())
	}
	if err != nil {
		return err
	}

	pw := viper.Get("writer").(io.WriteCloser)
	w := new(tabwriter.Writer)
	w.Init(pw, 0, 8, 1, '\t', 0)
	fmt.Fprintf(w, "%s\t%s\t%s\t%s\t%s\t\n", "Event ID", "Device", "Origin", "Created", "Modified")

	for _, event := range events {
		fmt.Fprintf(w, "%s\t%s\t%v\t%v\t%s\t\n",
			event.ID,
			event.Device,
			event.Origin,
			utils.DisplayDuration(event.Created),
			utils.DisplayDuration(event.Modified),
		)
	}
	w.Flush()
	return
}

func getEvent(client coredata.EventClient, id string) ([]models.Event, error) {
	event, err := client.Event(context.Background(), id)
	if err != nil {
		return nil, err
	}
	return []models.Event{event}, nil
}
