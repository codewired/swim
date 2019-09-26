// Copyright 2015-2019 SWIM.AI inc.
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

package swim.dataflow.windows;

import java.util.Optional;
import java.util.Set;
import swim.collections.HashTrieMap;

/**
 * {@link WindowAccumulators} backed by a simple map.
 * @param <W> The type of the windows.
 * @param <S> The type of the states.
 */
public class MapWindowAccumulators<W, S> implements WindowAccumulators<W, S> {

  private HashTrieMap<W, S> accumulators;

  public MapWindowAccumulators(final HashTrieMap<W, S> map) {
    accumulators = map;
  }

  public MapWindowAccumulators() {
    this(HashTrieMap.empty());
  }

  @Override
  public Optional<S> getForWindow(final W window) {
    return Optional.ofNullable(accumulators.get(window));
  }

  @Override
  public void updateWindow(final W window, final S state) {
    accumulators = accumulators.updated(window, state);
  }

  @Override
  public void removeWindow(final W window) {
    accumulators = accumulators.removed(window);
  }

  @Override
  public Set<W> windows() {
    return accumulators.keySet();
  }

  @Override
  public void close() {

  }
}