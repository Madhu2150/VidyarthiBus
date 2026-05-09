const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.database();

// FR-06: Auto-purge stale crowd reports older than 15 minutes
exports.purgeStaleReports = functions.pubsub
  .schedule("every 5 minutes")
  .onRun(async (_context) => {
    const ttlMs = 15 * 60 * 1000;
    const cutoff = Date.now() - ttlMs;
    const snapshot = await db.ref("crowd_reports").once("value");

    const deletions = [];
    snapshot.forEach((routeSnap) => {
      routeSnap.forEach((reportSnap) => {
        const report = reportSnap.val();
        if (report && report.timestamp < cutoff) {
          deletions.push(reportSnap.ref.remove());
        }
      });
    });

    await Promise.all(deletions);
    console.log(`Purged ${deletions.length} stale reports`);
    return null;
  });

// FR-08: Notify when bus becomes full (RED status)
const CAPACITY = 60;

exports.notifyOnCrowdChange = functions.database
  .ref("/crowd_reports/{routeId}")
  .onWrite(async (change, context) => {
    const { routeId } = context.params;

    const ttlMs = 15 * 60 * 1000;
    const cutoff = Date.now() - ttlMs;

    const reports = change.after.val() || {};
    const count = Object.values(reports).filter(
      (r) => r && r.timestamp > cutoff
    ).length;

    const isRed = count / CAPACITY > 0.70;

    const prev = change.before.val() || {};
    const prevCount = Object.values(prev).filter(
      (r) => r && r.timestamp > cutoff
    ).length;
    const wasRed = prevCount / CAPACITY > 0.70;

    if (isRed && !wasRed) {
      const routeSnap = await db.ref(`routes/${routeId}`).once("value");
      const route = routeSnap.val();
      const routeName = route ? route.routeName : `Route ${routeId}`;

      await admin.messaging().send({
        notification: {
          title: `Bus ${routeName} is FULL`,
          body: "Tap to view alternate transport options.",
        },
        topic: `route_${routeId}`,
      });
    }
    return null;
  });

// FR-09: Notify when bus is cancelled
exports.notifyOnCancellation = functions.database
  .ref("/routes/{routeId}/isCancelled")
  .onUpdate(async (change, context) => {
    const { routeId } = context.params;
    if (!change.after.val()) return null;

    const routeSnap = await db.ref(`routes/${routeId}`).once("value");
    const route = routeSnap.val();
    const routeName = route ? route.routeName : `Route ${routeId}`;

    await admin.messaging().send({
      notification: {
        title: `${routeName} Cancelled`,
        body: route?.cancellationMessage || "Please find alternate transport.",
      },
      topic: `route_${routeId}`,
    });
    return null;
  });