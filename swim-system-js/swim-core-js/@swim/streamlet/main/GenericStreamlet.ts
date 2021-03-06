// Copyright 2015-2020 Swim inc.
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

import {Inlet} from "./Inlet";
import {Outlet} from "./Outlet";
import {Streamlet} from "./Streamlet";

export interface GenericStreamlet<I = unknown, O = I> extends Streamlet<I, O> {
  getOutput(outlet: Outlet<O>): O | undefined;

  willDecohereInlet(inlet: Inlet<I>): void;

  didDecohereInlet(inlet: Inlet<I>): void;

  willRecohereInlet(inlet: Inlet<I>, version: number): void;

  didRecohereInlet(inlet: Inlet<I>, version: number): void;

  willDecohereOutlet(outlet: Outlet<O>): void;

  didDecohereOutlet(outlet: Outlet<O>): void;

  willRecohereOutlet(outlet: Outlet<O>, version: number): void;

  didRecohereOutlet(outlet: Outlet<O>, version: number): void;
}
