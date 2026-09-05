package com.sonexa.app.data.provider

import com.sonexa.app.data.model.*

class MusicRabbitHoleEngine(
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) {

    suspend fun exploreRabbitHole(seed: String, depth: Int = 1): RabbitHoleGraph {
        val rootNode = RabbitHoleNode(
            id = "node_root",
            title = seed,
            subtitle = "Starting Point",
            type = RabbitHoleNodeType.ARTIST,
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600"
        )

        val nodes = mutableListOf(rootNode)
        val edges = mutableListOf<RabbitHoleEdge>()

        when {
            seed.contains("Arijit", ignoreCase = true) || seed.contains("Tum Hi Ho", ignoreCase = true) -> {
                val pritam = RabbitHoleNode("node_pritam", "Pritam", "Legendary Music Director & Composer", RabbitHoleNodeType.COMPOSER, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600", trivia = "Has composed 200+ chartbusters with Arijit Singh")
                val rahman = RabbitHoleNode("node_rahman", "A.R. Rahman", "Oscar Winning Maestro", RabbitHoleNodeType.COMPOSER, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600", trivia = "Pritam and Rahman share classical Sufi and Indian raga roots")
                val sufi = RabbitHoleNode("node_sufi", "Sufi & Qawwali Roots", "Spiritual Harmonic Tradition", RabbitHoleNodeType.GENRE, "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600", trivia = "The harmonic foundation of modern Indian romantic melodies")
                val nusrat = RabbitHoleNode("node_nusrat", "Nusrat Fateh Ali Khan", "Shahenshah-e-Qawwali", RabbitHoleNodeType.ARTIST, "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600", trivia = "Influenced vocal improvisation in contemporary Hindi music")

                nodes.addAll(listOf(pritam, rahman, sufi, nusrat))
                edges.add(RabbitHoleEdge(rootNode.id, pritam.id, RabbitHoleRelation.COMPOSED_BY, "Pritam composed Arijit's breakthrough tracks"))
                edges.add(RabbitHoleEdge(pritam.id, rahman.id, RabbitHoleRelation.SIMILAR_TO, "Shared melodic complexity & orchestrations"))
                edges.add(RabbitHoleEdge(rahman.id, sufi.id, RabbitHoleRelation.GENRE_ROOT, "Deep foundation in Sufi musical roots"))
                edges.add(RabbitHoleEdge(sufi.id, nusrat.id, RabbitHoleRelation.INFLUENCED_BY, "Master of Sufi devotional improvisation"))
            }
            seed.contains("Diljit", ignoreCase = true) || seed.contains("Punjabi", ignoreCase = true) -> {
                val folk = RabbitHoleNode("node_punjabi_folk", "Bhangra & Tumbi Folk Roots", "Traditional Punjabi Acoustic Heritage", RabbitHoleNodeType.GENRE, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600")
                val kuldeep = RabbitHoleNode("node_kuldeep", "Kuldeep Manak", "Folk Legend", RabbitHoleNodeType.ARTIST, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600", trivia = "Pioneer of modern narrative Punjabi folk")
                val snoh = RabbitHoleNode("node_snoh", "Snoh Aalegra / Global R&B", "Contemporary Hip-Hop & Soul Collaborations", RabbitHoleNodeType.SIMILAR_ARTIST, "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600", trivia = "Collaborated on international crossover tracks")

                nodes.addAll(listOf(folk, kuldeep, snoh))
                edges.add(RabbitHoleEdge(rootNode.id, folk.id, RabbitHoleRelation.GENRE_ROOT, "Built upon traditional folk rhythms"))
                edges.add(RabbitHoleEdge(folk.id, kuldeep.id, RabbitHoleRelation.INFLUENCED_BY, "Inspired high-octave vocal delivery"))
                edges.add(RabbitHoleEdge(rootNode.id, snoh.id, RabbitHoleRelation.PERFORMED_BY, "Global genre fusion"))
            }
            else -> {
                val comp = RabbitHoleNode("node_comp", "Melodic Acoustic Producers", "Sound Design & Instrumentation", RabbitHoleNodeType.PRODUCER, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600")
                val roots = RabbitHoleNode("node_roots", "Indie Folk Traditions", "Acoustic Storytelling", RabbitHoleNodeType.GENRE, "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600")
                nodes.addAll(listOf(comp, roots))
                edges.add(RabbitHoleEdge(rootNode.id, comp.id, RabbitHoleRelation.PRODUCED_BY, "Acoustic arrangement"))
                edges.add(RabbitHoleEdge(comp.id, roots.id, RabbitHoleRelation.GENRE_ROOT, "Folk origins"))
            }
        }

        val streamTracks = aggregationEngine.searchAll(seed).tracks.take(5)
        val enrichedNodes = nodes.mapIndexed { idx, n ->
            n.copy(streamTrack = streamTracks.getOrNull(idx % streamTracks.size))
        }

        return RabbitHoleGraph(
            rootId = rootNode.id,
            rootTitle = seed,
            nodes = enrichedNodes,
            edges = edges,
            depth = depth,
            narrative = "Explore the lineage from '$seed' across composers, production styles, samples, and historical traditions."
        )
    }
}
