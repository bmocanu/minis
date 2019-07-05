/*
Copyright 2019 Bogdan Mocanu (https://bogdan.mocanu.ws)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ws.mocanu.minis.profiler;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A link from a code execution tree. During a profiling session, several of these links are created and
 * aggregated into a tree, which is later traversed in order to print a beautiful report.
 */
public class ExecLink {

    private final String point;
    private final Set<ExecLink> children;

    public ExecLink(String point) {
        this.point = point;
        this.children = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecLink execLink = (ExecLink) o;
        return point.equals(execLink.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point);
    }

    public String getPoint() {
        return point;
    }

    public Set<ExecLink> getChildren() {
        return children;
    }

    public void addChild(ExecLink link) {
        children.add(link);
    }
}
