/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.symmetric.notification;

import java.util.List;

import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.IncomingBatch;
import org.jumpmind.symmetric.model.Notification;
import org.jumpmind.symmetric.model.OutgoingBatch;
import org.jumpmind.symmetric.model.OutgoingBatches;
import org.jumpmind.symmetric.service.IIncomingBatchService;
import org.jumpmind.symmetric.service.IOutgoingBatchService;

public class NotificationCheckBatchError implements INotificationCheck, ISymmetricEngineAware {

    protected IOutgoingBatchService outgoingBatchService;
    
    protected IIncomingBatchService incomingBatchService;

    @Override
    public String getType() {
        return "batchError";
    }
    
    @Override
    public long check(Notification notification) {
        int outgoingErrorCount = 0;
        OutgoingBatches outgoingBatches = outgoingBatchService.getOutgoingBatchErrors(1000);
        for (OutgoingBatch batch : outgoingBatches.getBatches()) {
            int batchErrorMinutes = (int) (System.currentTimeMillis() - batch.getCreateTime().getTime()) / 60000;
            if (batchErrorMinutes >= notification.getThreshold()) {
                outgoingErrorCount++;
            }
        }

        int incomingErrorCount = 0;
        List<IncomingBatch> incomingBatches = incomingBatchService.findIncomingBatchErrors(1000);
        for (IncomingBatch batch : incomingBatches) {
            int batchErrorMinutes = (int) (System.currentTimeMillis() - batch.getCreateTime().getTime()) / 60000;
            if (batchErrorMinutes >= notification.getThreshold()) {
                incomingErrorCount++;
            }
        }

        return outgoingErrorCount + incomingErrorCount;
    }

    @Override
    public boolean shouldLockCluster() {
        return true;
    }

    @Override
    public boolean requiresPeriod() {
        return false;
    }

    @Override
    public void setSymmetricEngine(ISymmetricEngine engine) {
        outgoingBatchService = engine.getOutgoingBatchService();
        incomingBatchService = engine.getIncomingBatchService();
    }

}
