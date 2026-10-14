const express = require('express');
const router = express.Router();
const { tracks } = require('../data/catalog');

/**
 * Amazon Alexa Skill Webhook Handler for Zynera Audio
 * POST /api/v1/alexa
 */
router.post('/', (req, res) => {
  const body = req.body || {};
  const request = body.request || {};
  const requestType = request.type || 'Unknown';
  const userId = body.session?.user?.userId || body.context?.System?.user?.userId || 'anonymous_alexa_user';

  console.log(`[Alexa Skill] Received request: type=${requestType}, user=${userId}`);

  if (requestType === 'LaunchRequest') {
    return res.json(buildSpeechResponse(
      "Welcome to Zynera Audio. You can ask me to play a song, an artist, an album, or an audiobook. What would you like to listen to?",
      false
    ));
  }

  if (requestType === 'IntentRequest') {
    const intent = request.intent || {};
    const intentName = intent.name || '';

    if (intentName === 'PlayContentIntent' || intentName === 'PlayMusicIntent') {
      const query = intent.slots?.query?.value || intent.slots?.song?.value || intent.slots?.artist?.value || 'Kesariya';
      const matchedTrack = tracks.find(t => t.title?.toLowerCase().includes(query.toLowerCase()) || t.artist?.toLowerCase().includes(query.toLowerCase())) || tracks[0];

      return res.json(buildAudioPlayerPlayResponse(
        `Playing ${matchedTrack.title} on Zynera Audio.`,
        matchedTrack.id,
        matchedTrack.audioUrl || `https://cdn.zynera.app/audio/stream/${matchedTrack.id}.mp3`,
        0,
        matchedTrack.title,
        matchedTrack.artist,
        matchedTrack.coverUrl
      ));
    }

    if (intentName === 'SearchContentIntent') {
      const query = intent.slots?.query?.value || 'trending songs';
      return res.json(buildSpeechResponse(
        `I found matching songs for ${query} on Zynera Audio. The top track is ${tracks[0].title}. Would you like to play it?`,
        false
      ));
    }

    if (intentName === 'PlayPlaylistIntent') {
      const first = tracks[0];
      return res.json(buildAudioPlayerPlayResponse(
        "Playing your playlist on Zynera Audio.",
        first.id,
        first.audioUrl,
        0,
        first.title,
        first.artist,
        first.coverUrl
      ));
    }

    if (intentName === 'AMAZON.PauseIntent' || intentName === 'PauseIntent') {
      return res.json(buildAudioPlayerStopResponse("Music paused."));
    }

    if (intentName === 'AMAZON.ResumeIntent' || intentName === 'ResumeIntent') {
      const first = tracks[0];
      return res.json(buildAudioPlayerPlayResponse(
        "Resuming playback.",
        first.id,
        first.audioUrl,
        0,
        first.title,
        first.artist,
        first.coverUrl
      ));
    }

    if (intentName === 'AMAZON.NextIntent') {
      const next = tracks[1] || tracks[0];
      return res.json(buildAudioPlayerPlayResponse(
        `Playing next song: ${next.title}`,
        next.id,
        next.audioUrl,
        0,
        next.title,
        next.artist,
        next.coverUrl
      ));
    }

    if (intentName === 'AMAZON.PreviousIntent') {
      const first = tracks[0];
      return res.json(buildAudioPlayerPlayResponse(
        "Restarting track.",
        first.id,
        first.audioUrl,
        0,
        first.title,
        first.artist,
        first.coverUrl
      ));
    }

    if (intentName === 'AMAZON.StopIntent' || intentName === 'AMAZON.CancelIntent') {
      return res.json(buildAudioPlayerStopResponse(null));
    }

    if (intentName === 'AMAZON.HelpIntent') {
      return res.json(buildSpeechResponse(
        "You can say: play Kesariya, play relaxing music, resume, pause, next, or play my playlist. What would you like to do?",
        false
      ));
    }

    return res.json(buildSpeechResponse(
      "Zynera Audio didn't quite catch that. You can say: play a song, or search music.",
      false
    ));
  }

  // AudioPlayer events
  if (requestType.startsWith('AudioPlayer.')) {
    return res.json({
      version: '1.0',
      response: { shouldEndSession: true }
    });
  }

  return res.json(buildSpeechResponse("Welcome to Zynera Audio. Ask me to play any song or artist.", false));
});

function buildSpeechResponse(text, shouldEndSession) {
  return {
    version: '1.0',
    response: {
      outputSpeech: { type: 'PlainText', text },
      card: { type: 'Simple', title: 'Zynera Audio', content: text },
      reprompt: shouldEndSession ? undefined : {
        outputSpeech: { type: 'PlainText', text: 'What would you like to listen to next?' }
      },
      shouldEndSession
    }
  };
}

function buildAudioPlayerPlayResponse(speechText, token, streamUrl, offsetMs, title, artist, artUrl) {
  return {
    version: '1.0',
    response: {
      outputSpeech: speechText ? { type: 'PlainText', text: speechText } : undefined,
      directives: [
        {
          type: 'AudioPlayer.Play',
          playBehavior: 'REPLACE_ALL',
          audioItem: {
            stream: {
              token,
              url: streamUrl,
              offsetInMilliseconds: offsetMs
            },
            metadata: {
              title: title || 'Zynera Audio',
              subtitle: artist || 'Zynera',
              art: artUrl ? { sources: [{ url: artUrl }] } : undefined
            }
          }
        }
      ],
      shouldEndSession: true
    }
  };
}

function buildAudioPlayerStopResponse(speechText) {
  return {
    version: '1.0',
    response: {
      outputSpeech: speechText ? { type: 'PlainText', text: speechText } : undefined,
      directives: [{ type: 'AudioPlayer.Stop' }],
      shouldEndSession: true
    }
  };
}

module.exports = router;
