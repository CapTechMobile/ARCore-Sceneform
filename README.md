# ARCore-Sceneform

This is a demo of the core functionality found `Sceneform`. This demo illustrates the following `Sceneform` and ARCore topics/classes. To do so, the application found here uses a simple 3D "Post-It" Note. 

- `Scene` - a class used to maintain a scene graph, or a hierarchical organization of content in the currently known world. 

- `HitResult` -  derived from testing hits (touches) or points from the `scene` to determine if the specific point in space can be tracked.

- `Trackable` - something that ARCore can track and that `Anchors` can be attached to. 

- `Node` - represents a transformation within the scene graph's hierarchy, and can have 3D objects that belong to it. Thus a `node`, is how that object looks from a given perspective. A `node` can belong to the scene or other `nodes` that belong in the scene's hierarchy. 
 
- `Anchor` - used to describe a fixed location and orientation in the real world. An `Anchor` can be used as a point of reference for `nodes` to maintain a physical location.

- `TransformableNode` - a `node` that can be selected, translated, rotated, and scaled using gestures. This can be used to transform how objects appear in a physical space. 

- `Renderable` - a class used to render objects and widgets in a 3D space. 




![a gif of some code in the ide](images/demo_gif.gif)


# License
Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.