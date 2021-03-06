// Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.collide.client.util;

/**
 * Random utility methods on the client.
 */
public class Utils {

  /**
   * Compares two objects.
   *
   * @return {@code true} if the objects are same of equal,
   *         or {@code false} otherwise
   */
  public static boolean equalsOrNull(Object a, Object b) {
    return (a != null) ? a.equals(b) : (b == null);
  }
}
